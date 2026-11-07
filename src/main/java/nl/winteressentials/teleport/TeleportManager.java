package nl.winteressentials.teleport;

import net.kyori.adventure.title.Title;
import nl.winteressentials.WinterEssentials;
import nl.winteressentials.cooldown.CooldownManager;
import nl.winteressentials.message.MessageService;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {

    public static final String BYPASS_PERMISSION = "winteressentials.bypass.teleportdelay";

    private final WinterEssentials plugin;
    private final MessageService messageService;
    private final CooldownManager cooldownManager;

    private final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Location> startLocations = new ConcurrentHashMap<>();

    public TeleportManager(@NotNull WinterEssentials plugin,
                           @NotNull MessageService messageService,
                           @NotNull CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.messageService = messageService;
        this.cooldownManager = cooldownManager;
    }

    public void startTeleport(@NotNull Player player, @NotNull String homeName, @NotNull Location targetLocation) {
        UUID uuid = player.getUniqueId();

        cancel(player, null);

        int warmupSeconds = plugin.getConfig().getInt("teleport.warmup_seconds", 5);

        if (warmupSeconds <= 0 || player.hasPermission(BYPASS_PERMISSION) || player.isOp()) {
            executeTeleport(player, homeName, targetLocation);
            return;
        }

        startLocations.put(uuid, player.getLocation().clone());
        messageService.send(player, "teleporting", "home", homeName);

        BukkitTask task = new BukkitRunnable() {
            int ticksPassed = 0;
            int secondsLeft = warmupSeconds;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelTask(uuid);
                    return;
                }

                if (plugin.getConfig().getBoolean("teleport.particles", true)) {
                    Location loc = player.getLocation().add(0, 0.8, 0);
                    player.getWorld().spawnParticle(
                            Particle.SNOWFLAKE,
                            loc,
                            3,
                            0.3, 0.4, 0.3,
                            0.01
                    );
                }

                if (ticksPassed % 20 == 0) {
                    if (secondsLeft <= 0) {
                        cancelTask(uuid);
                        executeTeleport(player, homeName, targetLocation);
                        return;
                    }

                    sendCountdownDisplay(player, secondsLeft);

                    if (plugin.getConfig().getBoolean("teleport.sound", true)) {
                        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.4f, 1.4f);
                    }

                    secondsLeft--;
                }

                ticksPassed += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);

        activeTasks.put(uuid, task);
    }

    private void sendCountdownDisplay(@NotNull Player player, int secondsLeft) {
        String secondsStr = String.valueOf(secondsLeft);

        String actionbarRaw = plugin.getConfig().getString("messages.teleport_countdown_actionbar", "<#80D8FF>Teleportatie in <white>{seconds}s<#80D8FF>...");
        player.sendActionBar(messageService.parse(actionbarRaw, "seconds", secondsStr));

        String titleRaw = plugin.getConfig().getString("messages.teleport_countdown_title", "");
        String subtitleRaw = plugin.getConfig().getString("messages.teleport_countdown_subtitle", "");
        if (!titleRaw.isBlank() || !subtitleRaw.isBlank()) {
            Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ofMillis(300));
            Title title = Title.title(
                    messageService.parse(titleRaw, "seconds", secondsStr),
                    messageService.parse(subtitleRaw, "seconds", secondsStr),
                    times
            );
            player.showTitle(title);
        }
    }

    private void executeTeleport(@NotNull Player player, @NotNull String homeName, @NotNull Location targetLocation) {
        startLocations.remove(player.getUniqueId());
        activeTasks.remove(player.getUniqueId());

        player.clearTitle();
        player.sendActionBar(net.kyori.adventure.text.Component.empty());

        player.teleportAsync(targetLocation).thenAccept(success -> {
            if (success) {
                if (plugin.getConfig().getBoolean("teleport.particles", true)) {
                    player.getWorld().spawnParticle(
                            Particle.SNOWFLAKE,
                            targetLocation.clone().add(0, 1.0, 0),
                            20,
                            0.4, 0.5, 0.4,
                            0.05
                    );
                }
                if (plugin.getConfig().getBoolean("teleport.sound", true)) {
                    player.playSound(targetLocation, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.6f, 1.2f);
                }
                messageService.send(player, "teleport_success", "home", homeName);
                cooldownManager.applyCooldown(player, "home");
            }
        });
    }

    public void onPlayerMove(@NotNull Player player, @NotNull Location to) {
        if (!plugin.getConfig().getBoolean("teleport.cancel_on_move", true)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (!activeTasks.containsKey(uuid)) {
            return;
        }

        Location start = startLocations.get(uuid);
        if (start == null || !start.getWorld().equals(to.getWorld())) {
            cancel(player, "teleport_cancelled");
            return;
        }

        double dx = start.getX() - to.getX();
        double dy = start.getY() - to.getY();
        double dz = start.getZ() - to.getZ();
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq > 0.25) {
            cancel(player, "teleport_cancelled");
        }
    }

    public void onPlayerDamage(@NotNull Player player) {
        if (!plugin.getConfig().getBoolean("teleport.cancel_on_damage", true)) {
            return;
        }

        if (isTeleporting(player.getUniqueId())) {
            cancel(player, "teleport_cancelled");
        }
    }

    public boolean isTeleporting(@NotNull UUID uuid) {
        return activeTasks.containsKey(uuid);
    }

    public void cancel(@NotNull Player player, @org.jetbrains.annotations.Nullable String messageKey) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = activeTasks.remove(uuid);
        startLocations.remove(uuid);

        if (task != null) {
            task.cancel();
            player.clearTitle();
            player.sendActionBar(net.kyori.adventure.text.Component.empty());

            if (messageKey != null) {
                messageService.send(player, messageKey);
                if (plugin.getConfig().getBoolean("teleport.sound", true)) {
                    player.playSound(player.getLocation(), Sound.BLOCK_SNOW_BREAK, 0.5f, 0.8f);
                }
            }
        }
    }

    private void cancelTask(@NotNull UUID uuid) {
        BukkitTask task = activeTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        startLocations.remove(uuid);
    }

    public void cancelAll() {
        for (BukkitTask task : activeTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        activeTasks.clear();
        startLocations.clear();
    }
}
