package nl.winteressentials.cooldown;

import nl.winteressentials.WinterEssentials;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    public static final String BYPASS_PERMISSION = "winteressentials.bypass.cooldown";

    private final WinterEssentials plugin;

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public CooldownManager(@NotNull WinterEssentials plugin) {
        this.plugin = plugin;
    }

    public long getRemainingSeconds(@NotNull Player player, @NotNull String commandKey) {
        if (player.hasPermission(BYPASS_PERMISSION) || player.isOp()) {
            return 0;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return 0;
        }

        Long expireTime = playerCooldowns.get(commandKey.toLowerCase());
        if (expireTime == null) {
            return 0;
        }

        long now = System.currentTimeMillis();
        if (now >= expireTime) {
            playerCooldowns.remove(commandKey.toLowerCase());
            return 0;
        }

        long diffMillis = expireTime - now;
        return (diffMillis + 999) / 1000;
    }

    public boolean isOnCooldown(@NotNull Player player, @NotNull String commandKey) {
        return getRemainingSeconds(player, commandKey) > 0;
    }

    public void applyCooldown(@NotNull Player player, @NotNull String commandKey) {
        if (player.hasPermission(BYPASS_PERMISSION) || player.isOp()) {
            return;
        }

        int durationSeconds = getCooldownSecondsFromConfig(commandKey);
        if (durationSeconds <= 0) {
            return;
        }

        setCooldown(player, commandKey, durationSeconds);
    }

    public void setCooldown(@NotNull Player player, @NotNull String commandKey, int seconds) {
        if (seconds <= 0 || player.hasPermission(BYPASS_PERMISSION) || player.isOp()) {
            return;
        }

        long expireTime = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(commandKey.toLowerCase(), expireTime);
    }

    public void clear(@NotNull UUID uuid) {
        cooldowns.remove(uuid);
    }

    public int getCooldownSecondsFromConfig(@NotNull String commandKey) {
        if (plugin.getConfig().contains("commands." + commandKey + ".cooldown")) {
            return plugin.getConfig().getInt("commands." + commandKey + ".cooldown", 0);
        }
        return plugin.getConfig().getInt("cooldowns." + commandKey, 0);
    }
}
