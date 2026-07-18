package nl.chimpgamer.mapartlock.listener;

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import nl.chimpgamer.mapartlock.config.Messages;
import nl.chimpgamer.mapartlock.config.Settings;
import nl.chimpgamer.mapartlock.lock.MapLockService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

/**
 * Protects map art that already hangs on a wall.
 *
 * <p>Placing a map into an empty frame is never touched, so building keeps working. Every other
 * route to the item — punching it out, rotating it, breaking the frame, blowing it up — runs the
 * same ownership check, so protection never depends on one particular event firing.
 */
public final class ItemFrameProtectionListener implements Listener {
    private final Settings settings;
    private final MapLockService service;
    private final Messages messages;

    public ItemFrameProtectionListener(Settings settings, MapLockService service, Messages messages) {
        this.settings = settings;
        this.service = service;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFrameChange(PlayerItemFrameChangeEvent event) {
        if (!settings.protectItemFrames()
                || event.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE) {
            return;
        }

        if (!service.isLocked(event.getItemFrame().getItem())) {
            return;
        }

        if (service.mayUse(event.getItemFrame().getItem(), event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
        messages.send(event.getPlayer(), "item_frame_protected");
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (!holdsLockedMap(event.getEntity())) {
            return;
        }

        Player breaker = event instanceof HangingBreakByEntityEvent byEntity
                ? resolvePlayer(byEntity.getRemover())
                : null;
        denyUnlessAllowed(event, event.getEntity(), breaker);
    }

    /** Punching a frame pops the map out without breaking the frame itself. */
    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!holdsLockedMap(event.getEntity())) {
            return;
        }

        Player damager = event instanceof EntityDamageByEntityEvent byEntity
                ? resolvePlayer(byEntity.getDamager())
                : null;
        denyUnlessAllowed(event, event.getEntity(), damager);
    }

    private void denyUnlessAllowed(Cancellable event, Entity entity, Player player) {
        if (!(entity instanceof ItemFrame frame)) {
            return;
        }

        // No player means nobody who could hold the bypass permission, so deny outright.
        if (player != null && service.mayUse(frame.getItem(), player)) {
            return;
        }

        event.setCancelled(true);
        if (player != null) {
            messages.send(player, "item_frame_protected");
        }
    }

    /** Resolves an arrow or snowball back to whoever fired it. */
    private Player resolvePlayer(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }
        if (entity instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        return null;
    }

    private boolean holdsLockedMap(Entity entity) {
        return settings.protectItemFrames()
                && entity instanceof ItemFrame frame
                && service.isLocked(frame.getItem());
    }
}
