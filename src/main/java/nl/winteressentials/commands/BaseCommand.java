package nl.winteressentials.commands;

import nl.winteressentials.WinterEssentials;
import nl.winteressentials.cooldown.CooldownManager;
import nl.winteressentials.message.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    protected final WinterEssentials plugin;
    protected final MessageService messageService;
    protected final CooldownManager cooldownManager;
    protected final String commandKey;
    protected final String permission;
    protected final boolean playerOnly;

    public BaseCommand(@NotNull WinterEssentials plugin,
                       @NotNull String commandKey,
                       @Nullable String permission,
                       boolean playerOnly) {
        this.plugin = plugin;
        this.messageService = plugin.getMessageService();
        this.cooldownManager = plugin.getCooldownManager();
        this.commandKey = commandKey;
        this.permission = permission;
        this.playerOnly = playerOnly;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!isCommandEnabled()) {
            messageService.send(sender, "command_disabled");
            return true;
        }

        if (playerOnly && !(sender instanceof Player)) {
            messageService.send(sender, "only_players");
            return true;
        }

        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            messageService.send(sender, "no_permission");
            return true;
        }

        if (sender instanceof Player player) {
            long remaining = cooldownManager.getRemainingSeconds(player, commandKey);
            if (remaining > 0) {
                messageService.send(player, "on_cooldown", "time", MessageService.formatDuration(remaining));
                return true;
            }
        }

        return execute(sender, label, args);
    }

    public boolean isCommandEnabled() {
        return plugin.getConfig().getBoolean("commands." + commandKey + ".enabled", true);
    }

    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args);

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        return Collections.emptyList();
    }
}
