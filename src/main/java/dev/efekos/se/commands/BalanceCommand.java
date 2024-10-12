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
import dev.efekos.arn.common.annotation.block.BlockCommandBlock;
import dev.efekos.arn.common.annotation.block.BlockConsole;
import dev.efekos.se.StandardEconomy;
import dev.efekos.se.impl.EconomyProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static dev.efekos.se.StandardEconomy.format;

@Container
public class BalanceCommand {

    @Command("bal")
    public int bal(CommandSender sender, @CommandArgument("target") Player target) {
        EconomyProvider provider = StandardEconomy.getProvider();
        double balance = provider.getBalance(target);
        boolean self = sender instanceof Player p && p.getUniqueId().equals(target.getUniqueId());
        sender.sendMessage(format(self ? "balance-self" : "balance-other",
                Placeholder.component("amount", provider.createComponent(balance)),
                Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target))
        ));
        return 0;
    }

    @Command("balance")
    public int balance(CommandSender sender, @CommandArgument("target") Player target) {
        return bal(sender, target);
    }

    @Command("money")
    public int money(CommandSender sender, @CommandArgument("target") Player target) {
        return bal(sender, target);
    }

    @Command("bal")
    @BlockCommandBlock
    @BlockConsole
    public int bal(Player sender){
        return bal(sender,sender);
    }


    @Command("money")
    @BlockCommandBlock
    @BlockConsole
    public int money(Player sender){
        return bal(sender,sender);
    }

    @Command("balance")
    @BlockCommandBlock
    @BlockConsole
    public int balance(Player sender){
        return bal(sender,sender);
    }

}