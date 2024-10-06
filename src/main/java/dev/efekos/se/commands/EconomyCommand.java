package dev.efekos.se.commands;


import dev.efekos.se.StandardEconomy;
import dev.efekos.se.impl.EconomyProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class EconomyCommand implements BrigaiderCommand {

    @Override
    public void register(Commands commodore) {
        commodore.register(Commands.literal("economy")
                        .requires(o -> o.getSender().hasPermission("economy.admin"))
                .then(Commands.literal("set")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .then(Commands.argument("amount", ArgumentTypes.doubleRange())
                                        .executes(commandContext -> set(commandContext.getSource(),commandContext.getArgument("target", Player.class),commandContext.getArgument("amount",Double.class)))
                                )
                        )
                ).build());
    }

    private int set(CommandSourceStack source, Player target, Double amount) {
        EconomyProvider provider = StandardEconomy.getProvider();
        provider.setBalance(target,amount);
        source.getSender().sendMessage(NamedTextColor.YELLOW+"Changed "+NamedTextColor.AQUA+target.getName()+NamedTextColor.YELLOW+" to "+NamedTextColor.GREEN+provider.format(amount));
        target.sendMessage(NamedTextColor.YELLOW+"Your balance has been changed to "+NamedTextColor.GREEN+provider.format(amount));
        return 0;
    }

}
