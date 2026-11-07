package nl.winteressentials.commands;

import nl.winteressentials.WinterEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WinterEssentialsCommand extends BaseCommand {

    public WinterEssentialsCommand(@NotNull WinterEssentials plugin) {
        super(plugin, "winteressentials", "winteressentials.admin", false);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("winteressentials.admin.reload")) {
                messageService.send(sender, "no_permission");
                return true;
            }

            plugin.reloadPluginConfig();
            messageService.send(sender, "reload_success");
            return true;
        }

        messageService.sendRaw(sender, "<gradient:#80D8FF:#ECEFF1><bold>WinterEssentials</bold></gradient> <gray>v" + plugin.getPluginMeta().getVersion());
        messageService.sendRaw(sender, "<gray>Ontwikkeld door <#80D8FF>TotemSMP</#80D8FF> | Modern Paper/Purpur essentials");
        messageService.sendRaw(sender, "<gray>Typ <#80D8FF>/" + label + " reload</#80D8FF> om de configuratie te herladen.");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("winteressentials.admin.reload") && "reload".startsWith(args[0].toLowerCase())) {
                list.add("reload");
            }
            if ("version".startsWith(args[0].toLowerCase())) {
                list.add("version");
            }
            return list;
        }
        return Collections.emptyList();
    }
}
