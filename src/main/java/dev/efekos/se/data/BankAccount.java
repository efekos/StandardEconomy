package dev.efekos.se.data;

import dev.efekos.simple_ql.annotation.Primary;
import dev.efekos.simple_ql.data.AdaptedList;
import dev.efekos.simple_ql.data.Table;
import dev.efekos.simple_ql.data.TableRow;
import dev.efekos.simple_ql.implementor.PrimitiveImplementors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BankAccount extends TableRow<BankAccount> {

    public BankAccount(Class<BankAccount> clazz, Table<BankAccount> parentTable) {
        super(clazz, parentTable);
    }

    private AdaptedList<UUID> members = new AdaptedList<>(new ArrayList<>(), PrimitiveImplementors.UUID);
    private UUID owner;
    @Primary
    private String name;
    private double balance;

    public List<OfflinePlayer> getMembers() {
        return members.stream().map(Bukkit::getOfflinePlayer).toList();
    }

    public void setMembers(List<OfflinePlayer> members) {
        this.members = new AdaptedList<>(members.stream().map(OfflinePlayer::getUniqueId).toList(), PrimitiveImplementors.UUID);
        markDirty("members");
    }

    public void addMember(UUID id) {
        this.members.add(id);
        markDirty("members");
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
        markDirty("balance");
    }

    public void addMember(OfflinePlayer player) {
        this.members.add(player.getUniqueId());
        markDirty("members");
    }

    public OfflinePlayer getOwner() {
        return Bukkit.getOfflinePlayer(owner);
    }

    public void setOwner(OfflinePlayer owner) {
        this.owner = owner.getUniqueId();
        markDirty("owner");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        markDirty("name");
    }

}
