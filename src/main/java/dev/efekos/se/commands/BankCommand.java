package dev.efekos.se.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.efekos.se.StandardEconomy;
import dev.efekos.se.data.Bank;
import dev.efekos.se.impl.EconomyProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

import static dev.efekos.se.StandardEconomy.format;

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
                                .then(Commands.literal("all")
                                        .executes(ctx -> depositAll(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
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
                        .then(Commands.literal("balance")
                                .executes(commandContext -> balance(commandContext.getSource()))
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(commandContext -> balance(commandContext.getSource(),StringArgumentType.getString(commandContext,"name")))
                                )
                        )
                .build()
        );
    }

    private int balance(CommandSourceStack source, String name) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        EconomyResponse response = provider.bankBalance(name);
        if(!response.transactionSuccess()){
            p.sendMessage(format("<red>Could not find bank '<name>'",Placeholder.unparsed("name",name)));
            return 1;
        }
        p.sendMessage(format("<yellow><bank>'s balance: <amount>",Placeholder.component("bank",Component.text(name,NamedTextColor.AQUA)),Placeholder.component("amount",provider.createComponent(response.balance))));
        return 0;
    }

    private int balance(CommandSourceStack source) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("<red>You don't own a bank"));
            return 1;
        }
        p.sendMessage(format("<yellow><bank>'s balance: <amount>",Placeholder.component("bank",bank.toComponent()),Placeholder.component("amount",provider.createComponent(bank.balance()))));
        return 0;
    }

    private int depositAll(CommandSourceStack source, String name) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        double amount = provider.getBalance(p);
        if (!provider.isBankMember(name, p).transactionSuccess()) {
            p.sendMessage(format("<red>This bank does not exist or you are not a member of it."));
            return 1;
        }
        provider.withdrawPlayer(p, amount);
        provider.bankDeposit(name, amount);
        p.sendMessage(format("<yellow>Successfully deposited <amount> into <bank>.", Placeholder.component("bank", Component.text(name, NamedTextColor.AQUA)), Placeholder.component("amount", provider.createComponent(amount))));
        return 0;
    }

    private int payAll(CommandSourceStack source, Player target) {
        return 0;
    }

    private int pay(CommandSourceStack source, double amount, Player target) {
        return 0;
    }

    private int withdrawAll(CommandSourceStack source) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("<red>You don't own a bank"));
            return 1;
        }
        provider.bankWithdraw(bank.name(), bank.balance());
        provider.depositPlayer(p, bank.balance());
        p.sendMessage(format("<yellow>Successfully withdrew <amount> from <bank>",
                Placeholder.component("amount", provider.createComponent(bank.balance())), Placeholder.component("bank", bank.toComponent())));
        return 0;
    }

    private int deleteBank(CommandSourceStack source) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("<red>You don't own a bank"));
            return 1;
        }
        provider.depositPlayer(p, bank.balance());
        for (UUID member : bank.members()) {
            OfflinePlayer ofp = Bukkit.getOfflinePlayer(member);
            if (ofp.isOnline())
                ofp.getPlayer().sendMessage(format("<yellow>One of the banks you are a member of, <bank> just got deleted. The balance of the bank has been sent to bank owner.", Placeholder.component("bank", bank.toComponent())));
        }

        p.sendMessage(format("<yellow>Successfully deleted your bank. <amount> that was in the bank has been added to your balance.", Placeholder.component("amount", provider.createComponent(bank.balance()))));
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
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        EconomyResponse res = provider.withdrawPlayer(p, amount);
        if (!res.transactionSuccess()) {
            p.sendMessage(format("<red>You don't have <amount>", Placeholder.component("amount", provider.createComponent(amount))));
            return 1;
        }

        if (!provider.isBankMember(name, p).transactionSuccess()) {
            p.sendMessage(format("<red>This bank does not exist or you are not a member of it."));
            return 1;
        }
        provider.bankDeposit(name, amount);
        p.sendMessage(format("<yellow>Successfully deposited <amount> into <bank>.", Placeholder.component("bank", Component.text(name, NamedTextColor.AQUA)), Placeholder.component("amount", provider.createComponent(amount))));
        return 0;
    }

    private int withdraw(CommandSourceStack source, double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("<red>You don't own a bank"));
            return 1;
        }
        EconomyResponse res = provider.bankWithdraw(bank.name(), amount);
        if(!res.transactionSuccess()){
            p.sendMessage(format("<red><bank> does not have <amount>",Placeholder.component("amount", provider.createComponent(amount)),Placeholder.component("bank",bank.toComponent())));
            return 1;
        }
        provider.depositPlayer(p, amount);
        p.sendMessage(format("<yellow>Successfully withdrew <amount> from <bank>",
                Placeholder.component("amount", provider.createComponent(amount)), Placeholder.component("bank", bank.toComponent())));
        return 0;
    }

    private int createBank(CommandSourceStack source, String name) {
        Player p = (Player) source.getSender();
        EconomyProvider provider = StandardEconomy.getProvider();
        EconomyResponse response = provider.createBank(name, p);
        if (response.type != EconomyResponse.ResponseType.SUCCESS) {
            p.sendMessage(format("<red>Could not create a bank: <message>", Placeholder.unparsed("message", response.errorMessage)));
            return 1;
        }
        p.sendMessage(format("<green>Successfully created a bank!"));
        return 0;
    }

}
