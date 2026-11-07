package nl.winteressentials.listeners;

import nl.winteressentials.cooldown.CooldownManager;
import nl.winteressentials.managers.HomeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerConnectionListener implements Listener {

    private final HomeManager homeManager;
    private final CooldownManager cooldownManager;

    public PlayerConnectionListener(@NotNull HomeManager homeManager,
                                    @NotNull CooldownManager cooldownManager) {
        this.homeManager = homeManager;
        this.cooldownManager = cooldownManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {

        homeManager.preloadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();
        homeManager.unloadPlayer(uuid);
        cooldownManager.clear(uuid);
    }
}
