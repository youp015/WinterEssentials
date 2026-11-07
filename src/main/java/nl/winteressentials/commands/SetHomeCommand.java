package nl.winteressentials.commands;

import nl.winteressentials.WinterEssentials;
import nl.winteressentials.managers.HomeManager;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class SetHomeCommand extends BaseCommand {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,24}$");
    private final HomeManager homeManager;

    public SetHomeCommand(@NotNull WinterEssentials plugin, @NotNull HomeManager homeManager) {
        super(plugin, "sethome", "winteressentials.command.sethome", true);
        this.homeManager = homeManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        String homeName = args.length >= 1 ? args[0] : "home";

        if (!NAME_PATTERN.matcher(homeName).matches()) {
            messageService.sendRaw(player, "<red>Homenaam mag alleen letters, cijfers, streepjes en underscores bevatten (max 24 tekens).");
            return true;
        }

        homeManager.setHome(player, homeName, player.getLocation()).thenAccept(result -> {
            if (result.success()) {
                messageService.send(player, "home_set", "home", homeName);
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.5f, 1.3f);
                cooldownManager.applyCooldown(player, commandKey);
            } else {
                messageService.send(player, "home_limit_reached",
                        "limit", String.valueOf(result.limit()));
            }
        });

        return true;
    }
}
