package dev.efekos.se.impl;

import dev.efekos.se.StandardEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

public class EconomyProvider implements Economy {

    private final StandardEconomy parent;
    private final NumberFormat format;

    public EconomyProvider(StandardEconomy parent) {
        this.parent = parent;
        this.format = new DecimalFormat(parent.getCurrencySymbol()+"#0."+"0".repeat(parent.getFractionalDigits()));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "StandardEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return parent.areBanksEnabled();
    }

    @Override
    public int fractionalDigits() {
        return parent.getFractionalDigits();
    }

    @Override
    public String format(double amount) {
        return format.format(amount);
    }

    @Override
    public String currencyNamePlural() {
        return parent.getCurrencyName(true);
    }

    @Override
    public String currencyNameSingular() {
        return parent.getCurrencyName(false);
    }

    @Override
    public boolean hasAccount(String playerName) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return 0;
    }

    @Override
    public double getBalance(String playerName, String world) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return 0;
    }

    @Override
    public boolean has(String playerName, double amount) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return false;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return false;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        if(!parent.areBanksEnabled()) return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"Banks are disabled.");
        return null;
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        if(!parent.areBanksEnabled()) return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"Banks are disabled.");
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        if(!parent.areBanksEnabled()) return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"Banks are disabled.");
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        if(!parent.areBanksEnabled()) return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"Banks are disabled.");
        return null;
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        if(!parent.areBanksEnabled()) return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"Banks are disabled.");
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        if(!parent.areBanksEnabled()) return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"Banks are disabled.");
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        if(!parent.areBanksEnabled()) return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"Banks are disabled.");
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        if(!parent.areBanksEnabled()) return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"Banks are disabled.");
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        if(!parent.areBanksEnabled()) return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"Banks are disabled.");
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        if(!parent.areBanksEnabled()) return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"Banks are disabled.");
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        if(!parent.areBanksEnabled()) return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"Banks are disabled.");
        return null;
    }

    @Override
    public List<String> getBanks() {
        if(!parent.areBanksEnabled()) return Collections.emptyList();
        return List.of();
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }
}