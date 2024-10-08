package dev.efekos.se.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

public class BankCommand implements BrigaiderCommand {

    @Override
    public void register(Commands commands) {
        commands.register(Commands.literal("bank")
                .requires(commandSourceStack -> commandSourceStack.getSender() instanceof Player)
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> createBank(context.getSource(), StringArgumentType.getString(context, "name")))
                        )
                )
                .then(Commands.literal("delete")
                        .executes(commandContext -> deleteBank(commandContext.getSource()))
                )
                .then(Commands.literal("withdraw")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(commandContext -> withdraw(commandContext.getSource(), DoubleArgumentType.getDouble(commandContext, "amount")))
                        )
                        .then(Commands.literal("all")
                                .executes(commandContext -> withdrawAll(commandContext.getSource()))
                        )
                )
                .then(Commands.literal("pay")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(commandContext -> pay(commandContext.getSource(), DoubleArgumentType.getDouble(commandContext, "amount"), commandContext.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(commandContext.getSource()).getFirst()))
                                )
                                .then(Commands.literal("all")
                                        .executes(commandContext -> payAll(commandContext.getSource(), commandContext.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(commandContext.getSource()).getFirst()))
                                )
                        )
                )
                .then(Commands.literal("deposit")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(ctx -> deposit(ctx.getSource(), StringArgumentType.getString(ctx, "name"), DoubleArgumentType.getDouble(ctx, "amount")))
                                )
                        )
                )
                .then(Commands.literal("invite")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .executes(commandContext -> invite(commandContext.getSource(), commandContext.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(commandContext.getSource()).getFirst()))
                        )
                )
                .then(Commands.literal("kick")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .executes(commandContext -> kick(commandContext.getSource(), commandContext.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(commandContext.getSource()).getFirst()))
                        )
                )
                .then(Commands.literal("accept")
                        .executes(commandContext -> accept(commandContext.getSource()))
                )
                .then(Commands.literal("reject")
                        .executes(commandContext -> reject(commandContext.getSource()))
                )
                .build()
        );
    }

    private int payAll(CommandSourceStack source, Player target) {
        return 0;
    }

    private int pay(CommandSourceStack source, double amount, Player target) {
        return 0;
    }

    private int withdrawAll(CommandSourceStack source) {
        return 0;
    }

    private int deleteBank(CommandSourceStack source) {
        return 0;
    }

    private int reject(CommandSourceStack source) {
        return 0;
    }

    private int accept(CommandSourceStack source) {
        return 0;
    }

    private int kick(CommandSourceStack source, Player target) {
        return 0;
    }

    private int invite(CommandSourceStack source, Player target) {
        return 0;
    }

    private int deposit(CommandSourceStack source, String name, double amount) {
        return 0;
    }

    private int withdraw(CommandSourceStack source, double amount) {
        return 0;
    }

    private int createBank(CommandSourceStack source, String name) {
        return 0;
    }

}
