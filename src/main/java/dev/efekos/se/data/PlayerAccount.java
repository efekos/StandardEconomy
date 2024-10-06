package dev.efekos.se.data;

import dev.efekos.simple_ql.annotation.Primary;
import dev.efekos.simple_ql.data.Table;
import dev.efekos.simple_ql.data.TableRow;

import java.util.UUID;

public class PlayerAccount extends TableRow<PlayerAccount> {

    @Primary
    private UUID id;
    private String name;
    private double balance;

    public PlayerAccount(Class<PlayerAccount> clazz, Table<PlayerAccount> parentTable) {
        super(clazz, parentTable);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
        markDirty("id");
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
        markDirty("balance");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        markDirty("name");
    }
}
