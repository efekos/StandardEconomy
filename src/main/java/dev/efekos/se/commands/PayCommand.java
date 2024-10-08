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

import com.mojang.brigadier.arguments.DoubleArgumentType;
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

public class PayCommand implements BrigadierCommand {

    @Override
    public void register(Commands commands) {
        commands.register(Commands.literal("pay")
                .requires(commandSourceStack -> commandSourceStack.getSender() instanceof Player)
                .then(Commands.argument("target", ArgumentTypes.player())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> pay(((Player) ctx.getSource().getSender()), ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst(), DoubleArgumentType.getDouble(ctx, "amount")))
                        )
                )
                .build()
        );
    }

    private int pay(Player sender, Player target, Double amount) {
        if(target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(format("pay.self"));
            return 1;
        }
        EconomyProvider provider = StandardEconomy.getProvider();
        EconomyResponse res = provider.withdrawPlayer(sender, amount);
        if (!res.transactionSuccess()) {
            sender.sendMessage(format("pay.cant-afford",Placeholder.component("amount",provider.createComponent(amount))));
            return 1;
        }
        provider.depositPlayer(target, amount);
        target.sendMessage(format("pay.notification",
                Placeholder.component("sender", Component.text(sender.getName(), NamedTextColor.AQUA).hoverEvent(sender)),
                Placeholder.component("amount", provider.createComponent(amount))
        ));
        sender.sendMessage(format("pay.success",
                Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target)),
                Placeholder.component("amount", provider.createComponent(amount))
        ));
        return 0;
    }

}