/*
 * MIT License
 *
 * Copyright (c) 2024 efekos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.efekos.se.commands;

import dev.efekos.arn.common.annotation.Command;
import dev.efekos.arn.common.annotation.CommandArgument;
import dev.efekos.arn.common.annotation.Container;
import dev.efekos.arn.common.annotation.Description;
import dev.efekos.arn.common.annotation.block.BlockCommandBlock;
import dev.efekos.arn.common.annotation.block.BlockConsole;
import dev.efekos.arn.common.annotation.modifier.Word;
import dev.efekos.se.StandardEconomy;
import dev.efekos.se.data.Bank;
import dev.efekos.se.impl.EconomyProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static dev.efekos.se.StandardEconomy.format;
import static dev.efekos.se.StandardEconomy.getKey;

@Container
@Description("Bank management.")
public class BankCommand {

    @Command("bank.balance")
    public int balance(CommandSender sender, @CommandArgument("bank") @Word String name) {
        EconomyProvider provider = StandardEconomy.getProvider();

        EconomyResponse response = provider.bankBalance(name);
        if (!response.transactionSuccess()) {
            sender.sendMessage(format("bank.errors.not-found", Placeholder.unparsed("name", name)));
            return -1;
        }
        sender.sendMessage(format("bank.balance", Placeholder.component("bank", Component.text(name, NamedTextColor.AQUA)), Placeholder.component("amount", provider.createComponent(response.balance))));
        return (int) response.balance;
    }

    @Command("bank.balance")
    @BlockConsole
    @BlockCommandBlock
    public int balance(Player p) {
        EconomyProvider provider = StandardEconomy.getProvider();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        p.sendMessage(format("bank.balance", Placeholder.component("bank", bank.toComponent()), Placeholder.component("amount", provider.createComponent(bank.balance()))));
        return (int) bank.balance();
    }

    @Command("bank.deposit.a:0:all")
    @BlockConsole
    @BlockCommandBlock
    public int depositAll(Player p, @CommandArgument("bank") @Word String name) {
        EconomyProvider provider = StandardEconomy.getProvider();

        double amount = provider.getBalance(p);
        if (!provider.isBankMember(name, p).transactionSuccess()) {
            p.sendMessage(format("bank.errors.not-owned", Placeholder.unparsed("name", name)));
            return 1;
        }
        provider.withdrawPlayer(p, amount);
        provider.bankDeposit(name, amount);
        p.sendMessage(format("bank.deposit", Placeholder.component("bank", Component.text(name, NamedTextColor.AQUA)), Placeholder.component("amount", provider.createComponent(amount))));
        return 0;
    }

    @Command("bank.pay.a:0:all")
    @BlockConsole
    @BlockCommandBlock
    public int payAll(Player p, @CommandArgument("target") Player target) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        if (bank.balance() == 0) {
            p.sendMessage(format("bank.errors.nothing", Placeholder.component("bank", bank.toComponent())));
            return 1;
        }
        provider.bankWithdraw(bank.name(), bank.balance());
        provider.depositPlayer(target, bank.balance());
        target.sendMessage(format("bank.pay.notification", Placeholder.component("amount", provider.createComponent(bank.balance())), Placeholder.component("bank", bank.toComponent())));
        p.sendMessage(format("bank.pay.success",
                Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA)), Placeholder.component("bank", bank.toComponent()),
                Placeholder.component("amount", provider.createComponent(bank.balance()))
        ));
        return 0;
    }

    @Command("bank.pay")
    @BlockConsole
    @BlockCommandBlock
    public int pay(Player p, @CommandArgument("target") Player target, @CommandArgument("amount") double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        if (bank.balance() < amount) {
            p.sendMessage(format("bank.errors.not-enough", Placeholder.component("amount", provider.createComponent(amount)), Placeholder.component("bank", bank.toComponent())));
            return 1;
        }
        provider.bankWithdraw(bank.name(), amount);
        provider.depositPlayer(target, amount);
        target.sendMessage(format("bank.pay.notification", Placeholder.component("amount", provider.createComponent(amount)), Placeholder.component("bank", bank.toComponent())));
        p.sendMessage(format("bank.pay.success",
                Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA)), Placeholder.component("bank", bank.toComponent()),
                Placeholder.component("amount", provider.createComponent(amount))
        ));
        return 0;
    }

    @Command("bank.withdraw.all")
    @BlockConsole
    @BlockCommandBlock
    public int withdrawAll(Player p) {
        EconomyProvider provider = StandardEconomy.getProvider();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        if (bank.balance() == 0) {
            p.sendMessage(format("bank.errors.nothing", Placeholder.component("bank", bank.toComponent())));
            return 1;
        }
        provider.bankWithdraw(bank.name(), bank.balance());
        provider.depositPlayer(p, bank.balance());
        p.sendMessage(format("bank.withdraw",
                Placeholder.component("amount", provider.createComponent(bank.balance())), Placeholder.component("bank", bank.toComponent())));
        return 0;
    }

    @Command("bank.delete")
    @BlockConsole
    @BlockCommandBlock
    public int deleteBank(Player p) {
        EconomyProvider provider = StandardEconomy.getProvider();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.not-owned"));
            return 1;
        }
        provider.depositPlayer(p, bank.balance());
        for (UUID member : bank.members()) {
            OfflinePlayer ofp = Bukkit.getOfflinePlayer(member);
            if (ofp.isOnline())
                Optional.ofNullable(ofp.getPlayer()).ifPresent(plr -> plr.sendMessage(format("bank.delete.notification", Placeholder.component("bank", bank.toComponent()))));
        }

        provider.deleteBank(bank.name());
        p.sendMessage(format("bank.delete.success", Placeholder.component("amount", provider.createComponent(bank.balance()))));
        return 0;
    }

    private static final Map<UUID, String> invites = new HashMap<>();

    @Command("bank.reject")
    @BlockConsole
    @BlockCommandBlock
    public int reject(Player p) {
        if (!invites.containsKey(p.getUniqueId())) {
            p.sendMessage(format("bank.errors.no-invite"));
            return 1;
        }
        String bankName = invites.get(p.getUniqueId());
        EconomyProvider provider = StandardEconomy.getProvider();
        Bank bank = provider.getBank(bankName);
        OfflinePlayer bankOwner = Bukkit.getOfflinePlayer(bank.owner());
        if (bankOwner.isOnline())
            Optional.ofNullable(bankOwner.getPlayer()).ifPresent(plr -> plr.sendMessage(format("bank.reject.notification",
                    Placeholder.component("target", Component.text(p.getName(), NamedTextColor.AQUA).hoverEvent(p))
            )));

        invites.remove(p.getUniqueId());
        p.sendMessage(format("bank.reject.success"));
        return 0;
    }

    @Command("bank.accept")
    @BlockConsole
    @BlockCommandBlock
    public int accept(Player p) {
        EconomyProvider provider = StandardEconomy.getProvider();
        if (!invites.containsKey(p.getUniqueId())) {
            p.sendMessage(format("bank.errors.no-invite"));
            return 1;
        }
        String bankName = invites.get(p.getUniqueId());
        OfflinePlayer bankOwner = Bukkit.getOfflinePlayer(provider.getBank(bankName).owner());
        if (bankOwner.isOnline())
            Optional.ofNullable(bankOwner.getPlayer()).ifPresent(plr -> plr.sendMessage(format("bank.accept.notification",
                    Placeholder.component("target", Component.text(p.getName(), NamedTextColor.AQUA).hoverEvent(p))
            )));

        invites.remove(p.getUniqueId());
        provider.addToBank(bankName, p);
        p.sendMessage(format("bank.accept.success"));
        return 0;
    }

    @Command("bank.kick")
    @BlockConsole
    @BlockCommandBlock
    public int kick(Player p, @CommandArgument("target") Player target) {
        EconomyProvider provider = StandardEconomy.getProvider();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        if (!bank.members().contains(target.getUniqueId())) {
            p.sendMessage(format("bank.errors.not-member", Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target))));
        }
        provider.removeFromBank(bank.name(), p);
        target.sendMessage(format("bank.kick.notification", Placeholder.component("bank", bank.toComponent())));
        p.sendMessage(format("bank.kick.success", Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target))));
        return 0;
    }

    @Command("bank.invite")
    @BlockConsole
    @BlockCommandBlock
    public int invite(Player p, @CommandArgument("target") Player target) {
        EconomyProvider provider = StandardEconomy.getProvider();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        invites.put(target.getUniqueId(), bank.name());
        target.sendMessage(format("bank.invite.notification",
                Placeholder.component("sender", Component.text(p.getName(), NamedTextColor.AQUA).hoverEvent(p)),
                Placeholder.component("bank", bank.toComponent()),
                Placeholder.component("yes_button", Component.text("[" + getKey("bank.invite.yes") + "]", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/bank accept"))),
                Placeholder.component("no_button", Component.text("[" + getKey("bank.invite.no") + "]", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/bank reject")))
        ));
        p.sendMessage(format("bank.invite.success",
                Placeholder.component("target", Component.text(target.getName()).hoverEvent(target))
        ));
        return 0;
    }

    @Command("bank.deposit")
    @BlockConsole
    @BlockCommandBlock
    public int deposit(Player p, @CommandArgument("bank") @Word String name, @CommandArgument("amount") double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();

        if (!provider.isBankMember(name, p).transactionSuccess()) {
            p.sendMessage(format("bank.errors.not-found", Placeholder.component("name", Component.text(name, NamedTextColor.AQUA))));
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

    @Command("bank.withdraw")
    @BlockConsole
    @BlockCommandBlock
    public int withdraw(Player p, @CommandArgument("amount") double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();

        Bank bank = provider.getBank(p);
        if (bank == null) {
            p.sendMessage(format("bank.errors.not-owned"));
            return 1;
        }
        EconomyResponse res = provider.bankWithdraw(bank.name(), amount);
        if (!res.transactionSuccess()) {
            p.sendMessage(format("bank.errors.not-enough", Placeholder.component("amount", provider.createComponent(amount)), Placeholder.component("bank", bank.toComponent())));
            return 1;
        }
        provider.depositPlayer(p, amount);
        p.sendMessage(format("bank.withdraw",
                Placeholder.component("amount", provider.createComponent(amount)), Placeholder.component("bank", bank.toComponent())));
        return 0;
    }

    @Command("bank.create")
    @BlockConsole
    @BlockCommandBlock
    public int createBank(Player p, @CommandArgument("name") @Word String name) {
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
