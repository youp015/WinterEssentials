package nl.winteressentials.commands;

import nl.winteressentials.WinterEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class FeedCommand extends BaseCommand {

    public FeedCommand(@NotNull WinterEssentials plugin) {
        super(plugin, "feed", "winteressentials.command.feed", false);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player target;

        if (args.length >= 1) {
            if (!sender.hasPermission("winteressentials.command.feed.others")) {
                messageService.send(sender, "no_permission");
                return true;
            }

            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                messageService.send(sender, "player_not_found");
                return true;
            }
        } else {
            if (!(sender instanceof Player playerSender)) {
                messageService.send(sender, "only_players");
                return true;
            }
            target = playerSender;
        }

        target.setFoodLevel(20);
        target.setSaturation(20.0f);
        target.setExhaustion(0.0f);
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.4f, 1.2f);

        if (sender instanceof Player p && p.equals(target)) {
            messageService.send(target, "fed");
            cooldownManager.applyCooldown(p, commandKey);
        } else {
            messageService.send(sender, "fed_other", "target", target.getName());
            messageService.send(target, "fed_by_other", "sender", sender.getName());
            if (sender instanceof Player p) {
                cooldownManager.applyCooldown(p, commandKey);
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("winteressentials.command.feed.others")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
