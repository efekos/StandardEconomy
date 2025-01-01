/*
 * MIT License
 *
 * Copyright (c) 2025 efekos
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
import dev.efekos.se.StandardEconomy;
import dev.efekos.se.impl.EconomyProvider;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.*;

import static dev.efekos.se.StandardEconomy.format;

@Container
@Description("Top 10 richest players.")
public class BalTopCommand {

    @Command("baltop")
    public int baltop(CommandSender sender){
        return baltop(sender,0);
    }

    @Command("baltop")
    public int baltop(CommandSender sender, @CommandArgument("page") int page) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Map<UUID, Double> ten = provider.getBalTop(page);

        if(ten.isEmpty()){
            sender.sendMessage(format(page==0?"baltop.not-enough":"baltop.too-deep"));
            return 1;
        }


        String pageString = (page + 1) + "";
        sender.sendMessage(format("baltop.header", Placeholder.unparsed("page", pageString)));

        Set<UUID> keys = ten.keySet();
        UUID[] uuids = keys.toArray(UUID[]::new);
        for (int i = 0; i < keys.size(); i++) {
            UUID id = uuids[i];
            OfflinePlayer player = Bukkit.getOfflinePlayer(id);
            Double amount = ten.get(id);

            sender.sendMessage(format("baltop.entry",
                    Placeholder.component("amount", provider.createComponent(amount)),
                    Placeholder.unparsed("place", (page*10+i+1)+""),
                    Placeholder.unparsed("name", Optional.ofNullable(player.getName()).orElse("?????"))
            ));

        }

        sender.sendMessage(format("baltop.footer", Placeholder.unparsed("page_adder","-".repeat(pageString.length()))));

        return 0;
    }

}
