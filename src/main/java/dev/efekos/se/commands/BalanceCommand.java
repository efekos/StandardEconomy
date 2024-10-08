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

public class BalanceCommand implements BrigadierCommand {

    @Override
    public void register(Commands commands) {
        commands.register(Commands.literal("bal")
                        .requires(source->source.getSender() instanceof Player)
                .executes(commandContext -> {
                    if (commandContext.getSource().getSender() instanceof Player p) bal(commandContext.getSource(), p);
                    return 0;
                })
                .then(Commands.argument("target", ArgumentTypes.player()).executes(commandContext -> bal(
                        commandContext.getSource(),
                        commandContext.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(commandContext.getSource()).getFirst())))
                .build(), List.of("balance", "money"));
    }

    private int bal(CommandSourceStack source, Player target) {
        EconomyProvider provider = StandardEconomy.getProvider();
        double balance = provider.getBalance(target);
        boolean self = source.getSender() instanceof Player p && p.getUniqueId().equals(target.getUniqueId());
        source.getSender().sendMessage(format(self ? "balance-self" : "balance-other",
                Placeholder.component("amount", provider.createComponent(balance)),
                Placeholder.component("target", Component.text(target.getName(), NamedTextColor.AQUA).hoverEvent(target))
        ));
        return 0;
    }
}