package nl.chimpgamer.mapartlock.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class Settings {
    private boolean openMenuOnSneakRightClick;
    private boolean glint;
    private boolean showOwnerInLore;
    private boolean syncLoreWhenHeld;
    private boolean protectAnvil;
    private boolean protectCraftingTable;
    private boolean protectCartographyTable;
    private boolean protectCrafter;
    private boolean protectItemFrames;
    private boolean protectAgainstHoppers;
    private boolean bypassEnabled;
    private boolean logActions;

    public Settings(FileConfiguration config) {
        reload(config);
    }

    public void reload(FileConfiguration config) {
        openMenuOnSneakRightClick = config.getBoolean("menu.open-on-sneak-right-click", true);

        glint = config.getBoolean("appearance.glint", true);
        showOwnerInLore = config.getBoolean("appearance.show-owner-in-lore", true);
        syncLoreWhenHeld = config.getBoolean("appearance.sync-lore-when-held", true);

        protectAnvil = config.getBoolean("protection.anvil", true);
        protectCraftingTable = config.getBoolean("protection.crafting-table", true);
        protectCartographyTable = config.getBoolean("protection.cartography-table", true);
        protectCrafter = config.getBoolean("protection.crafter", true);
        protectItemFrames = config.getBoolean("protection.item-frames", true);
        protectAgainstHoppers = config.getBoolean("protection.hoppers", true);

        bypassEnabled = config.getBoolean("admin.bypass-enabled", true);
        logActions = config.getBoolean("admin.log-actions", true);
    }

    public boolean openMenuOnSneakRightClick() {
        return openMenuOnSneakRightClick;
    }

    public boolean glint() {
        return glint;
    }

    public boolean showOwnerInLore() {
        return showOwnerInLore;
    }

    public boolean syncLoreWhenHeld() {
        return syncLoreWhenHeld;
    }

    public boolean protectAnvil() {
        return protectAnvil;
    }

    public boolean protectCraftingTable() {
        return protectCraftingTable;
    }

    public boolean protectCartographyTable() {
        return protectCartographyTable;
    }

    public boolean protectCrafter() {
        return protectCrafter;
    }

    public boolean protectItemFrames() {
        return protectItemFrames;
    }

    public boolean protectAgainstHoppers() {
        return protectAgainstHoppers;
    }

    /** Whether {@code mapartlock.bypass} does anything at all. */
    public boolean bypassEnabled() {
        return bypassEnabled;
    }

    public boolean logActions() {
        return logActions;
    }

    /** Human-readable summary of what is being protected, for the reload response. */
    public String activeProtections() {
        StringBuilder active = new StringBuilder();
        append(active, protectAnvil, "aambeeld");
        append(active, protectCraftingTable, "crafting");
        append(active, protectCartographyTable, "cartografietafel");
        append(active, protectCrafter, "crafter");
        append(active, protectItemFrames, "item frames");
        append(active, protectAgainstHoppers, "hoppers");
        return active.isEmpty() ? "geen" : active.toString();
    }

    private void append(StringBuilder target, boolean enabled, String name) {
        if (!enabled) {
            return;
        }
        if (!target.isEmpty()) {
            target.append(", ");
        }
        target.append(name);
    }
}
