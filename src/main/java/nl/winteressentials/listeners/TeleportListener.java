package nl.winteressentials.listeners;

import nl.winteressentials.teleport.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class TeleportListener implements Listener {

    private final TeleportManager teleportManager;

    public TeleportListener(@NotNull TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        if (event.hasChangedPosition() && teleportManager.isTeleporting(event.getPlayer().getUniqueId())) {
            teleportManager.onPlayerMove(event.getPlayer(), event.getTo());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(@NotNull EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            teleportManager.onPlayerDamage(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        teleportManager.cancel(event.getPlayer(), null);
    }
}
