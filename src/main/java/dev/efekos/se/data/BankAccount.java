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

    public void removeMember(OfflinePlayer player) {
        this.members.remove(player.getUniqueId());
        markDirty("members");
    }
}
