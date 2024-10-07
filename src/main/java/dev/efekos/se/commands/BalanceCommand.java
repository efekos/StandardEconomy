package dev.efekos.se.commands;

import dev.efekos.se.StandardEconomy;
import dev.efekos.se.impl.EconomyProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;

import static dev.efekos.se.StandardEconomy.format;

public class BalanceCommand implements BrigaiderCommand {

    @Override
    public void register(Commands commands) {
        commands.register(Commands.literal("bal")
                .then(Commands.argument("target", ArgumentTypes.player()).executes(commandContext -> bal(
                        commandContext.getSource(),
                        commandContext.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(commandContext.getSource()).getFirst())))
                .build(), List.of("balance", "money"));
    }

    private int bal(CommandSourceStack source, Player target) {
        EconomyProvider provider = StandardEconomy.getProvider();
        double balance = provider.getBalance(target);
        boolean self = source.getSender() instanceof Player p && p.getUniqueId().equals(target.getUniqueId());
        format(self ? "<yellow>Your balance: <amount>" : "<yellow><target>'s balance: <amount>",
                Placeholder.component("amount", Component.text(provider.format(balance), NamedTextColor.AQUA)),
                Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target))
        );
        return 0;
    }
}