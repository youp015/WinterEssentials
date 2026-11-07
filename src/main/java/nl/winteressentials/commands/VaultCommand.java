package nl.winteressentials.commands;

import nl.winteressentials.WinterEssentials;
import nl.winteressentials.managers.VaultManager;
import nl.winteressentials.permissions.LimitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VaultCommand extends BaseCommand {

    private final VaultManager vaultManager;
    private final LimitManager limitManager;

    public VaultCommand(@NotNull WinterEssentials plugin,
                        @NotNull VaultManager vaultManager,
                        @NotNull LimitManager limitManager) {
        super(plugin, "vault", "winteressentials.command.vault", true);
        this.vaultManager = vaultManager;
        this.limitManager = limitManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        int vaultNumber = 1;
        if (args.length >= 1) {
            try {
                vaultNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                messageService.sendRaw(player, "<red>Ongeldig nummer. Gebruik: /" + label + " <1-99>");
                return true;
            }
        }

        if (vaultNumber < 1 || vaultNumber > 99) {
            messageService.sendRaw(player, "<red>Kies een kluisnummer tussen 1 en 99.");
            return true;
        }

        int max = limitManager.getMaxVaults(player);
        if (vaultNumber > max) {
            messageService.send(player, "vault_limit_reached",
                    "number", String.valueOf(vaultNumber),
                    "limit", String.valueOf(max));
            return true;
        }

        vaultManager.openVault(player, vaultNumber);
        cooldownManager.applyCooldown(player, commandKey);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            int max = limitManager.getMaxVaults(player);
            List<String> list = new ArrayList<>();
            for (int i = 1; i <= Math.min(max, 25); i++) {
                String s = String.valueOf(i);
                if (s.startsWith(args[0])) {
                    list.add(s);
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}
