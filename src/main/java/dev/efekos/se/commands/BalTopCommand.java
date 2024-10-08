package dev.efekos.se.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.efekos.se.StandardEconomy;
import dev.efekos.se.impl.EconomyProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.*;

import static dev.efekos.se.StandardEconomy.format;

public class BalTopCommand implements BrigaiderCommand {

    @Override
    public void register(Commands commands) {
        commands.register(Commands.literal("baltop")
                        .executes(ctx -> baltop(ctx.getSource(),0))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1)).executes(ctx -> baltop(ctx.getSource(),IntegerArgumentType.getInteger(ctx,"page")-1)))
                .build());
    }

    private int baltop(CommandSourceStack source, int page) {
        EconomyProvider provider = StandardEconomy.getProvider();
        Map<UUID, Double> ten = provider.getBalTop(page);

        CommandSender sender = source.getSender();

        if(ten.isEmpty()){
            sender.sendMessage(format(page==0?"<red>Can't get anyone to be in top 10 yet.":"<red>Can't go that deep yet."));
            return 1;
        }


        String pageString = (page + 1) + "";
        sender.sendMessage(format("<dark_green>---------- <green>BalTop Page <page> <dark_green>----------", Placeholder.unparsed("page", pageString)));

        Set<UUID> keys = ten.keySet();
        UUID[] uuids = keys.toArray(UUID[]::new);
        for (int i = 0; i < keys.size(); i++) {
            UUID id = uuids[i];
            OfflinePlayer player = Bukkit.getOfflinePlayer(id);
            Double amount = ten.get(id);

            sender.sendMessage(format("<dark_green><place>. <green><name> <dark_green>- <amount>",
                    Placeholder.component("amount", provider.createComponent(amount)),
                    Placeholder.unparsed("place", (page*10+i+1)+""),
                    Placeholder.unparsed("name", player.getName())
            ));

        }

        sender.sendMessage(format("<dark_green>----------------------------------<page_adder>", Placeholder.unparsed("page_adder","-".repeat(pageString.length()))));

        return 0;
    }

}
