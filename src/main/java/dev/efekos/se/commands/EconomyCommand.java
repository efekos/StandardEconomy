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
import dev.efekos.se.StandardEconomy;
import dev.efekos.se.impl.EconomyProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static dev.efekos.se.StandardEconomy.format;

public class EconomyCommand  {

    @Command("eco.reset")
    public int reset(CommandSender sender,@CommandArgument("target") Player target) {
        return set(sender, target, StandardEconomy.getProvider().getDefaultBalance());
    }

    @Command("eco.remove")
    public int remove(CommandSender sender,@CommandArgument("target") Player target,@CommandArgument("double") Double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        provider.setBalance(target, provider.getBalance(target) - amount);
        sender.sendMessage(format("eco.remove.success", Placeholder.component("amount", provider.createComponent(amount)), Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target))));
        target.sendMessage(format("eco.remove.notification", Placeholder.component("amount", provider.createComponent(amount))));
        return 0;
    }

    @Command("eco.add")
    public int add(CommandSender sender,@CommandArgument("target") Player target,@CommandArgument("amount") Double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        provider.setBalance(target, provider.getBalance(target) + amount);
        sender.sendMessage(format("eco.add.success",
                Placeholder.component("amount", Component.text(provider.format(amount), NamedTextColor.GREEN)), Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target))
        ));
        target.sendMessage(format("eco.add.notification", Placeholder.component("amount", provider.createComponent(amount))));
        return 0;
    }

    @Command("eco.set")
    public int set(CommandSender sender, @CommandArgument("target") Player target,@CommandArgument("double") Double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        provider.setBalance(target, amount);
        sender.sendMessage(format("eco.change.success",
                Placeholder.component("amount", Component.text(provider.format(amount), NamedTextColor.GREEN)), Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target))));
        target.sendMessage(format("eco.change.notification", Placeholder.component("amount", provider.createComponent(amount))));
        return 0;
    }

}
