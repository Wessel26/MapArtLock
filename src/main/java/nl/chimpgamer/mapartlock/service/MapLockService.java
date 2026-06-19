package nl.chimpgamer.mapartlock.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.chimpgamer.mapartlock.config.PluginSettings;
import nl.chimpgamer.mapartlock.permission.MapLockPermissions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class MapLockService {
    private static final byte LOCKED = 1;
    private static final byte UNLOCKED = 0;
    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PluginSettings settings;
    private final NamespacedKey lockedKey;
    private final NamespacedKey ownerKey;
    private final NamespacedKey mapIdKey;
    private final NamespacedKey lockedAtKey;
    private final NamespacedKey originalLoreKey;

    public MapLockService(PluginSettings settings) {
        this.settings = settings;
        this.lockedKey = key("maplock:locked");
        this.ownerKey = key("maplock:owner");
        this.mapIdKey = key("maplock:map_id");
        this.lockedAtKey = key("maplock:locked_at");
        this.originalLoreKey = key("maplock:original_lore");
    }

    public MapLockService(JavaPlugin plugin, PluginSettings settings) {
        this(settings);
    }

    public boolean isFilledMap(ItemStack itemStack) {
        return itemStack != null && itemStack.getType() == Material.FILLED_MAP;
    }

    public boolean hasLockData(ItemStack itemStack) {
        if (!isFilledMap(itemStack)) {
            return false;
        }
        return itemStack.getPersistentDataContainer().has(lockedKey, PersistentDataType.BYTE)
                || itemStack.getPersistentDataContainer().has(ownerKey, PersistentDataType.STRING);
    }

    public boolean isLocked(ItemStack itemStack) {
        if (!isFilledMap(itemStack)) {
            return false;
        }

        Byte value = itemStack.getPersistentDataContainer().get(lockedKey, PersistentDataType.BYTE);
        return value != null && value == LOCKED;
    }

    public boolean hasOwner(ItemStack itemStack) {
        return ownerUuid(itemStack).isPresent();
    }

    public boolean canLock(ItemStack itemStack, Player player) {
        return !hasOwner(itemStack) || isOwner(itemStack, player) || MapLockPermissions.canManageOthers(player);
    }

    public void setOwner(ItemStack itemStack, Player owner) {
        if (!isFilledMap(itemStack)) {
            return;
        }

        String mapId = mapKey(itemStack).orElse(UUID.randomUUID().toString());
        itemStack.editPersistentDataContainer(container -> {
            container.set(ownerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
            container.set(mapIdKey, PersistentDataType.STRING, mapId);
            if (!container.has(lockedKey, PersistentDataType.BYTE)) {
                container.set(lockedKey, PersistentDataType.BYTE, UNLOCKED);
            }
        });
    }

    public void lock(ItemStack itemStack, Player owner) {
        if (!isFilledMap(itemStack)) {
            return;
        }

        long lockedAt = Instant.now().getEpochSecond();
        List<Component> originalLore = itemStack.hasItemMeta() ? itemStack.getItemMeta().lore() : null;
        String mapId = mapKey(itemStack).orElse(UUID.randomUUID().toString());

        UUID storedOwner = ownerUuid(itemStack).orElse(owner.getUniqueId());

        itemStack.editPersistentDataContainer(container -> {
            container.set(lockedKey, PersistentDataType.BYTE, LOCKED);
            container.set(ownerKey, PersistentDataType.STRING, storedOwner.toString());
            container.set(mapIdKey, PersistentDataType.STRING, mapId);
            container.set(lockedAtKey, PersistentDataType.LONG, lockedAt);
            container.set(originalLoreKey, PersistentDataType.STRING, serializeLore(originalLore));
        });

        itemStack.editMeta(meta -> applyLockedVisuals(meta, ownerName(storedOwner)));
    }

    public void unlock(ItemStack itemStack) {
        if (!isFilledMap(itemStack)) {
            return;
        }

        List<Component> originalLore = originalLore(itemStack).orElse(null);
        String mapId = mapKey(itemStack).orElse(UUID.randomUUID().toString());
        UUID owner = ownerUuid(itemStack).orElse(null);

        itemStack.editPersistentDataContainer(container -> {
            container.set(lockedKey, PersistentDataType.BYTE, UNLOCKED);
            container.set(mapIdKey, PersistentDataType.STRING, mapId);
            container.remove(lockedAtKey);
            container.remove(originalLoreKey);
            if (owner != null) {
                container.set(ownerKey, PersistentDataType.STRING, owner.toString());
            } else {
                container.remove(ownerKey);
            }
        });

        itemStack.editMeta(meta -> removeLockedVisuals(meta, originalLore));
    }

    public boolean canUnlock(ItemStack itemStack, Player player) {
        return MapLockPermissions.canManageOthers(player) || isOwner(itemStack, player);
    }

    public boolean canUseInProtectedInventory(ItemStack itemStack, Player player) {
        return !isLocked(itemStack) || isOwner(itemStack, player)
                || settings.adminBypassProtection() && MapLockPermissions.canBypassProtection(player);
    }

    public boolean isOwner(ItemStack itemStack, Player player) {
        return ownerUuid(itemStack)
                .map(owner -> owner.equals(player.getUniqueId()))
                .orElse(false);
    }

    public LockInfo lockInfo(ItemStack itemStack) {
        if (!isFilledMap(itemStack)) {
            return LockInfo.empty();
        }

        String mapId = mapKey(itemStack).orElse("-");
        long lockedAt = Optional.ofNullable(itemStack.getPersistentDataContainer().get(lockedAtKey, PersistentDataType.LONG)).orElse(0L);
        return new LockInfo(
                mapId,
                ownerUuid(itemStack).map(this::ownerName).orElse("-"),
                isLocked(itemStack),
                lockedAt
        );
    }

    public String formatLockedAt(long lockedAt) {
        if (lockedAt <= 0) {
            return "-";
        }
        return Instant.ofEpochSecond(lockedAt).atZone(ZoneId.systemDefault()).format(DATE_FORMATTER);
    }

    private Optional<String> mapKey(ItemStack itemStack) {
        if (!isFilledMap(itemStack) || !itemStack.hasItemMeta()) {
            return Optional.empty();
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof MapMeta mapMeta) {
            MapView mapView = mapMeta.getMapView();
            if (mapView != null) {
                return Optional.of(String.valueOf(mapView.getId()));
            }
        }

        String storedMapId = itemStack.getPersistentDataContainer().get(mapIdKey, PersistentDataType.STRING);
        return storedMapId == null || storedMapId.isBlank() ? Optional.empty() : Optional.of(storedMapId);
    }

    private Optional<UUID> ownerUuid(ItemStack itemStack) {
        if (!isFilledMap(itemStack)) {
            return Optional.empty();
        }

        var container = itemStack.getPersistentDataContainer();
        String value = container.get(ownerKey, PersistentDataType.STRING);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private String ownerName(UUID owner) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
        String name = offlinePlayer.getName();
        return name == null ? owner.toString() : name;
    }

    private void applyLockedVisuals(ItemMeta meta, String ownerName) {
        List<Component> lore = new ArrayList<>(Objects.requireNonNullElseGet(meta.lore(), List::of));
        removeProtectionLoreFallback(lore);
        lore.add(Component.text("MapArt Protection", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Status: Locked", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        if (settings.showOwnerInLore()) {
            lore.add(Component.text("Owner: " + ownerName, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);

        if (settings.enableGlint()) {
            meta.setEnchantmentGlintOverride(true);
        }
    }

    private void removeLockedVisuals(ItemMeta meta, List<Component> originalLore) {
        if (originalLore != null) {
            meta.lore(originalLore.isEmpty() ? null : originalLore);
        } else {
            List<Component> lore = new ArrayList<>(Objects.requireNonNullElseGet(meta.lore(), List::of));
            removeProtectionLoreFallback(lore);
            meta.lore(lore.isEmpty() ? null : lore);
        }
        meta.setEnchantmentGlintOverride(null);
    }

    private Optional<List<Component>> originalLore(ItemStack itemStack) {
        String value = itemStack.getPersistentDataContainer().get(originalLoreKey, PersistentDataType.STRING);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(deserializeLore(value));
    }

    private String serializeLore(List<Component> lore) {
        if (lore == null || lore.isEmpty()) {
            return "";
        }

        return lore.stream()
                .map(MINI_MESSAGE::serialize)
                .map(serialized -> Base64.getEncoder().encodeToString(serialized.getBytes(StandardCharsets.UTF_8)))
                .reduce((first, second) -> first + ";" + second)
                .orElse("");
    }

    private List<Component> deserializeLore(String value) {
        if (value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(";"))
                .filter(encoded -> !encoded.isBlank())
                .map(encoded -> new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8))
                .map(MINI_MESSAGE::deserialize)
                .toList();
    }

    private void removeProtectionLoreFallback(List<Component> lore) {
        lore.removeIf(this::isProtectionLoreLine);
    }

    private boolean isProtectionLoreLine(Component component) {
        String plain = PLAIN_TEXT.serialize(component);
        return plain.equals("MapArt Protection")
                || plain.equals("Status: Locked")
                || plain.startsWith("Owner: ");
    }

    private static NamespacedKey key(String value) {
        return Objects.requireNonNull(NamespacedKey.fromString(value), "Invalid key: " + value);
    }

    public record LockInfo(String mapId, String ownerName, boolean locked, long lockedAt) {
        private static LockInfo empty() {
            return new LockInfo("-", "-", false, 0L);
        }
    }
}
