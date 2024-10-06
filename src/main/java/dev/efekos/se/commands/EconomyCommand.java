package dev.efekos.se.commands;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.efekos.se.StandardEconomy;
import dev.efekos.se.impl.EconomyProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import me.lucko.commodore.Commodore;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class EconomyCommand implements CommondoreCommand{

    @Override
    public void register(Commodore commodore) {
        commodore.register(LiteralArgumentBuilder.literal("economy")
                        .requires(o ->
                            ((CommandSourceStack) o).getSender().hasPermission("economy.admin")
                        )
                .then(LiteralArgumentBuilder.literal("set")
                        .then(RequiredArgumentBuilder.argument("target", ArgumentTypes.player())
                                .then(RequiredArgumentBuilder.argument("amount", ArgumentTypes.doubleRange())
                                        .executes(commandContext -> set(((CommandSourceStack) commandContext.getSource()),commandContext.getArgument("target", Player.class),commandContext.getArgument("amount",Double.class)))
                                )
                        )
                )
        );
    }

    private int set(CommandSourceStack source, Player target, Double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        provider.setBalance(target,amount);
        source.getSender().sendMessage(NamedTextColor.YELLOW+"Changed "+NamedTextColor.AQUA+target.getName()+NamedTextColor.YELLOW+" to "+NamedTextColor.GREEN+provider.format(amount));
        target.sendMessage(NamedTextColor.YELLOW+"Your balance has been changed to "+NamedTextColor.GREEN+provider.format(amount));
        return 0;
    }

}
