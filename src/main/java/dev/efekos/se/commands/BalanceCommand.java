package dev.efekos.se.commands;

import dev.efekos.se.StandardEconomy;
import dev.efekos.se.impl.EconomyProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand extends Command {

    public BalanceCommand(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player p)) {
            commandSender.sendMessage(Component.text("This command can only be used by a player!", NamedTextColor.RED));
            return true;
        }

        EconomyProvider provider = StandardEconomy.getProvider();
        double balance = provider.getBalance(p);
        p.sendMessage(Component.join(
                JoinConfiguration.builder().separator(Component.space()).build(),
                Component.text("Your balance:", NamedTextColor.YELLOW),Component.text(provider.format(balance),NamedTextColor.GREEN)
        ));

        return true;
    }

}