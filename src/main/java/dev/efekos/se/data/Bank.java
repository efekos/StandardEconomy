package dev.efekos.se.data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public record Bank(List<UUID> members,UUID owner,String name,double balance) {

    public static Bank wrap(BankAccount account){
        if(account==null)return null;
        return new Bank(account.getMembers().stream().map(OfflinePlayer::getUniqueId).toList(),account.getOwner().getUniqueId(),account.getName(),account.getBalance());
    }

    public Component toComponent(){
        return Component.text(name, NamedTextColor.AQUA);
    }

}
