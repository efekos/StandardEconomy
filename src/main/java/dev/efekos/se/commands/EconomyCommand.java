package dev.efekos.se.commands;


import com.mojang.brigadier.arguments.DoubleArgumentType;
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

public class EconomyCommand implements BrigaiderCommand {

    @Override
    public void register(Commands commodore) {
        commodore.register(Commands.literal("economy")
                .requires(o -> o.getSender().hasPermission("economy.admin"))
                .then(Commands.literal("set")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(commandContext -> set(commandContext.getSource(), commandContext.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(commandContext.getSource()).getFirst(), commandContext.getArgument("amount", Double.class)))
                                )
                        )
                )
                .then(Commands.literal("add")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(commandContext -> add(commandContext.getSource(), commandContext.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(commandContext.getSource()).getFirst(), commandContext.getArgument("amount", Double.class)))
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(commandContext -> remove(commandContext.getSource(), commandContext.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(commandContext.getSource()).getFirst(), commandContext.getArgument("amount", Double.class)))
                                )
                        )
                )
                .then(Commands.literal("reset")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .executes(commandContext -> reset(commandContext.getSource(), commandContext.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(commandContext.getSource()).getFirst()))
                        )
                )
                .build(), List.of("eco"));
    }

    private int reset(CommandSourceStack source, Player target) {
        return set(source, target, StandardEconomy.getProvider().getDefaultBalance());
    }


    private int remove(CommandSourceStack source, Player target, Double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        provider.setBalance(target, provider.getBalance(target) - amount);
        source.getSender().sendMessage(format("eco.remove.success", Placeholder.component("amount", provider.createComponent(amount)), Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target))));
        target.sendMessage(format("eco.remove.notification", Placeholder.component("amount", provider.createComponent(amount))));
        return 0;
    }

    private int add(CommandSourceStack source, Player target, Double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        provider.setBalance(target, provider.getBalance(target) + amount);
        source.getSender().sendMessage(format("eco.add.success",
                Placeholder.component("amount", Component.text(provider.format(amount), NamedTextColor.GREEN)), Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target))
        ));
        target.sendMessage(format("eco.add.notification", Placeholder.component("amount", provider.createComponent(amount))));
        return 0;
    }

    private int set(CommandSourceStack source, Player target, Double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        provider.setBalance(target, amount);
        source.getSender().sendMessage(format("eco.change.success",
                Placeholder.component("amount", Component.text(provider.format(amount), NamedTextColor.GREEN)), Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target))));
        target.sendMessage(format("eco.change.notification", Placeholder.component("amount", provider.createComponent(amount))));
        return 0;
    }

}
