package nl.winteressentials.permissions;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import nl.winteressentials.WinterEssentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class LimitManager {

    private final WinterEssentials plugin;
    private boolean luckPermsAvailable = false;

    public LimitManager(@NotNull WinterEssentials plugin) {
        this.plugin = plugin;
        checkLuckPerms();
    }

    public void checkLuckPerms() {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                luckPermsAvailable = true;
                plugin.getLogger().info("LuckPerms integratie succesvol gedetecteerd en geactiveerd.");
            } else {
                luckPermsAvailable = false;
            }
        } catch (Throwable t) {
            luckPermsAvailable = false;
        }
    }

    public int getMaxVaults(@NotNull Player player) {
        if (player.hasPermission("winteressentials.vaults.unlimited") || player.hasPermission("winteressentials.vaults.*") || player.isOp()) {
            return 99;
        }

        int limit = getDynamicLimit(player, "vaults", plugin.getConfig().getInt("defaults.max_vaults", 1));
        return Math.min(Math.max(limit, 1), 99);
    }

    public int getMaxHomes(@NotNull Player player) {
        if (player.hasPermission("winteressentials.homes.unlimited") || player.hasPermission("winteressentials.homes.*") || player.isOp()) {
            return 1000;
        }

        int limit = getDynamicLimit(player, "homes", plugin.getConfig().getInt("defaults.max_homes", 1));
        return Math.max(limit, 1);
    }

    private int getDynamicLimit(@NotNull Player player, @NotNull String category, int defaultValue) {
        int max = defaultValue;
        String prefix = "winteressentials." + category.toLowerCase() + ".";

        if (luckPermsAvailable) {
            try {
                LuckPerms lp = LuckPermsProvider.get();
                User user = lp.getUserManager().getUser(player.getUniqueId());
                if (user != null) {

                    String metaVal = user.getCachedData().getMetaData().getMetaValue(category.toLowerCase());
                    if (metaVal != null) {
                        try {
                            int metaInt = Integer.parseInt(metaVal.trim());
                            if (metaInt > max) {
                                max = metaInt;
                            }
                        } catch (NumberFormatException ignored) {}
                    }

                    for (var node : user.getNodes()) {
                        if (node.getValue()) {
                            String key = node.getKey().toLowerCase();
                            if (key.startsWith(prefix)) {
                                String suffix = key.substring(prefix.length());
                                try {
                                    int val = Integer.parseInt(suffix);
                                    if (val > max) {
                                        max = val;
                                    }
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                plugin.getLogger().log(Level.FINE, "Could not query LuckPerms API", t);
            }
        }

        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (info.getValue()) {
                String node = info.getPermission().toLowerCase();
                if (node.startsWith(prefix)) {
                    String suffix = node.substring(prefix.length());
                    try {
                        int val = Integer.parseInt(suffix);
                        if (val > max) {
                            max = val;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return max;
    }
}
