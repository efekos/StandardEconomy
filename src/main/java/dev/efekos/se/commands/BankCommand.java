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
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static dev.efekos.se.StandardEconomy.format;
import static dev.efekos.se.StandardEconomy.getKey;

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
            p.sendMessage(format("bank.errors.not-found",Placeholder.unparsed("name",name)));
            return 1;
        }
        p.sendMessage(format("bank.balance",Placeholder.component("bank",Component.text(name,NamedTextColor.AQUA)),Placeholder.component("amount",provider.createComponent(response.balance))));
        return 0;
    }

    private int balance(CommandSourceStack source) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        p.sendMessage(format("bank.balance",Placeholder.component("bank",bank.toComponent()),Placeholder.component("amount",provider.createComponent(bank.balance()))));
        return 0;
    }

    private int depositAll(CommandSourceStack source, String name) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        double amount = provider.getBalance(p);
        if (!provider.isBankMember(name, p).transactionSuccess()) {
            p.sendMessage(format("bank.errors.not-owned",Placeholder.unparsed("name",name)));
            return 1;
        }
        provider.withdrawPlayer(p, amount);
        provider.bankDeposit(name, amount);
        p.sendMessage(format("bank.deposit", Placeholder.component("bank", Component.text(name, NamedTextColor.AQUA)), Placeholder.component("amount", provider.createComponent(amount))));
        return 0;
    }

    private int payAll(CommandSourceStack source, Player target) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = ((Player) source.getSender());
        Bank bank = provider.getBank(p);
        if(bank==null){
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        if(bank.balance()==0){
            p.sendMessage(format("bank.errors.nothing",Placeholder.component("bank",bank.toComponent())));
            return 1;
        }
        provider.bankWithdraw(bank.name(), bank.balance());
        provider.depositPlayer(target,bank.balance());
        target.sendMessage(format("bank.pay.notification",Placeholder.component("amount",provider.createComponent(bank.balance())),Placeholder.component("bank",bank.toComponent())));
        p.sendMessage(format("bank.pay.success",
                Placeholder.component("target",Component.text(target.getName(),NamedTextColor.AQUA)), Placeholder.component("bank",bank.toComponent()),
                Placeholder.component("amount",provider.createComponent(bank.balance()))
        ));
        return 0;
    }

    private int pay(CommandSourceStack source, double amount, Player target) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = ((Player) source.getSender());
        Bank bank = provider.getBank(p);
        if(bank==null){
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        if(bank.balance()<amount){
            p.sendMessage(format("bank.errors.not-enough",Placeholder.component("amount",provider.createComponent(amount)),Placeholder.component("bank",bank.toComponent())));
            return 1;
        }
        provider.bankWithdraw(bank.name(), amount);
        provider.depositPlayer(target,amount);
        target.sendMessage(format("bank.pay.notification",Placeholder.component("amount",provider.createComponent(amount)),Placeholder.component("bank",bank.toComponent())));
        p.sendMessage(format("bank.pay.success",
                Placeholder.component("target",Component.text(target.getName(),NamedTextColor.AQUA)), Placeholder.component("bank",bank.toComponent()),
                Placeholder.component("amount",provider.createComponent(amount))
        ));
        return 0;
    }

    private int withdrawAll(CommandSourceStack source) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        if(bank.balance()==0){
            p.sendMessage(format("bank.errors.nothing",Placeholder.component("bank",bank.toComponent())));
            return 1;
        }
        provider.bankWithdraw(bank.name(), bank.balance());
        provider.depositPlayer(p, bank.balance());
        p.sendMessage(format("bank.withdraw",
                Placeholder.component("amount", provider.createComponent(bank.balance())), Placeholder.component("bank", bank.toComponent())));
        return 0;
    }

    private int deleteBank(CommandSourceStack source) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.not-owned"));
            return 1;
        }
        provider.depositPlayer(p, bank.balance());
        for (UUID member : bank.members()) {
            OfflinePlayer ofp = Bukkit.getOfflinePlayer(member);
            if (ofp.isOnline())
                Optional.ofNullable(ofp.getPlayer()).ifPresent(plr->plr.sendMessage(format("bank.delete.notification", Placeholder.component("bank", bank.toComponent()))));
        }

        provider.deleteBank(bank.name());
        p.sendMessage(format("bank.delete.success", Placeholder.component("amount", provider.createComponent(bank.balance()))));
        return 0;
    }

    private static final Map<UUID,String> invites = new HashMap<>();

    private int reject(CommandSourceStack source) {
        Player p = (Player) source.getSender();
        if(!invites.containsKey(p.getUniqueId())){
            p.sendMessage(format("bank.errors.no-invite"));
            return 1;
        }
        String bankName = invites.get(p.getUniqueId());
        EconomyProvider provider = StandardEconomy.getProvider();
        Bank bank = provider.getBank(bankName);
        OfflinePlayer bankOwner = Bukkit.getOfflinePlayer(bank.owner());
        if(bankOwner.isOnline()) Optional.ofNullable(bankOwner.getPlayer()).ifPresent(plr->plr.sendMessage(format("bank.reject.notification",
                Placeholder.component("target",Component.text(p.getName(),NamedTextColor.AQUA).hoverEvent(p))
        )));

        invites.remove(p.getUniqueId());
        p.sendMessage(format("bank.reject.success"));
        return 0;
    }

    private int accept(CommandSourceStack source) {
        Player p = (Player) source.getSender();
        EconomyProvider provider = StandardEconomy.getProvider();
        if(!invites.containsKey(p.getUniqueId())){
            p.sendMessage(format("bank.errors.no-invite"));
            return 1;
        }
        String bankName = invites.get(p.getUniqueId());
        OfflinePlayer bankOwner = Bukkit.getOfflinePlayer(provider.getBank(bankName).owner());
        if(bankOwner.isOnline()) Optional.ofNullable(bankOwner.getPlayer()).ifPresent(plr->plr.sendMessage(format("bank.accept.notification",
                Placeholder.component("target",Component.text(p.getName(),NamedTextColor.AQUA).hoverEvent(p))
        )));

        invites.remove(p.getUniqueId());
        provider.addToBank(bankName,p);
        p.sendMessage(format("bank.accept.success"));
        return 0;
    }

    private int kick(CommandSourceStack source, Player target) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        if(!bank.members().contains(target.getUniqueId())){
            p.sendMessage(format("bank.errors.not-member",Placeholder.component("target",Component.text(target.getName(),NamedTextColor.AQUA).hoverEvent(target))));
        }
        provider.removeFromBank(bank.name(),p);
        target.sendMessage(format("bank.kick.notification",Placeholder.component("bank",bank.toComponent())));
        p.sendMessage(format("bank.kick.success",Placeholder.component("target",Component.text(target.getName(),NamedTextColor.AQUA).hoverEvent(target))));
        return 0;
    }

    private int invite(CommandSourceStack source, Player target) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        invites.put(target.getUniqueId(),bank.name());
        target.sendMessage(format("bank.invite.notification",
                Placeholder.component("sender",Component.text(p.getName(),NamedTextColor.AQUA).hoverEvent(p)),
                Placeholder.component("bank",bank.toComponent()),
                Placeholder.component("yes_button",Component.text("["+getKey("bank.invite.yes")+"]", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/bank accept"))),
                Placeholder.component("no_button",Component.text("["+getKey("bank.invite.no")+"]", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/bank reject")))
                ));
        p.sendMessage(format("bank.invite.success",
                Placeholder.component("target",Component.text(target.getName()).hoverEvent(target))
                ));
        return 0;
    }

    private int deposit(CommandSourceStack source, String name, double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        if (!provider.isBankMember(name, p).transactionSuccess()) {
            p.sendMessage(format("bank.errors.not-found",Placeholder.component("name",Component.text(name,NamedTextColor.AQUA))));
            return 1;
        }

        EconomyResponse res = provider.withdrawPlayer(p, amount);
        if (!res.transactionSuccess()) {
            p.sendMessage(format("pay.cant-afford", Placeholder.component("amount", provider.createComponent(amount))));
            return 1;
        }

        provider.bankDeposit(name, amount);
        p.sendMessage(format("bank.deposit", Placeholder.component("bank", Component.text(name, NamedTextColor.AQUA)), Placeholder.component("amount", provider.createComponent(amount))));
        return 0;
    }

    private int withdraw(CommandSourceStack source, double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Player p = (Player) source.getSender();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        EconomyResponse res = provider.bankWithdraw(bank.name(), amount);
        if(!res.transactionSuccess()){
            p.sendMessage(format("bank.errors.not-enough",Placeholder.component("amount", provider.createComponent(amount)),Placeholder.component("bank",bank.toComponent())));
            return 1;
        }
        provider.depositPlayer(p, amount);
        p.sendMessage(format("bank.withdraw",
                Placeholder.component("amount", provider.createComponent(amount)), Placeholder.component("bank", bank.toComponent())));
        return 0;
    }

    private int createBank(CommandSourceStack source, String name) {
        Player p = (Player) source.getSender();
        EconomyProvider provider = StandardEconomy.getProvider();
        EconomyResponse response = provider.createBank(name, p);
        if (response.type != EconomyResponse.ResponseType.SUCCESS) {
            p.sendMessage(format("bank.create.error", Placeholder.unparsed("message", response.errorMessage)));
            return 1;
        }
        p.sendMessage(format("bank.create.success"));
        return 0;
    }

}
