package dev.efekos.se.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.efekos.se.StandardEconomy;
import dev.efekos.se.impl.EconomyProvider;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

import static dev.efekos.se.StandardEconomy.format;

public class PayCommand implements BrigaiderCommand {

    @Override
    public void register(Commands commands) {
        commands.register(Commands.literal("pay")
                .requires(commandSourceStack -> commandSourceStack.getSender() instanceof Player)
                .then(Commands.argument("target", ArgumentTypes.player())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> pay(((Player) ctx.getSource().getSender()), ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst(), IntegerArgumentType.getInteger(ctx, "amount")))
                        )
                )
                .build()
        );
    }

    private int pay(Player sender, Player target, int amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        EconomyResponse res = provider.withdrawPlayer(sender, amount);
        if (!res.transactionSuccess()) {
            sender.sendMessage(format("<red>You do not have enough money."));
            return 1;
        }
        provider.depositPlayer(target, amount);
        target.sendMessage(format("<yellow><sender> just sent you <amount>!",
                Placeholder.component("sender", Component.text(sender.getName(), NamedTextColor.AQUA).hoverEvent(sender)),
                Placeholder.component("amount", Component.text(provider.format(amount), NamedTextColor.GREEN))
        ));
        sender.sendMessage(format("<yellow>Successfully sent <target> <amount>!",
                Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target)),
                Placeholder.component("amount", Component.text(provider.format(amount), NamedTextColor.GREEN))
        ));
        return 0;
    }

}