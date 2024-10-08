package dev.efekos.se.impl;

import dev.efekos.se.StandardEconomy;
import dev.efekos.se.data.BankAccount;
import dev.efekos.se.data.PlayerAccount;
import dev.efekos.se.data.TopTenCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class EconomyProvider implements Economy {

    private final StandardEconomy parent;
    private final NumberFormat format;
    private final TopTenCache cache = new TopTenCache(this::getTopTen);
    private final EconomyResponse BANK_DISABLED_RESPONSE = new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are disabled.");

    public EconomyProvider(StandardEconomy parent) {
        this.parent = parent;
        this.format = new DecimalFormat(parent.getCurrencySymbol() + "#,##0." + "0".repeat(parent.getFractionalDigits()));
    }

    private Map<UUID,Double> getTopTen(int page){
        List<PlayerAccount> ten = parent.getTopTen(page);
        Map<UUID,Double> topTen = new TreeMap<>();
        for (PlayerAccount account : ten) topTen.put(account.getId(),account.getBalance());
        return topTen;
    }

    public Map<UUID,Double> getBalTop(int page){
        return cache.get(page);
    }

    public void clearBalTopCache(){
        cache.clearCache();
    }

    public Component createComponent(double amount) {
        return Component.text(format.format(amount), NamedTextColor.GREEN).hoverEvent(Component.text(amount+" "+parent.getCurrencyName(amount!=1),NamedTextColor.GREEN));
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
        return Optional.ofNullable(parent.getAccount(playerName)).map(PlayerAccount::getBalance).orElse(0d);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return parent.getAccount(player).getBalance();
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        return withdrawPlayer(offlinePlayer, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        PlayerAccount account = parent.getAccount(player);
        if (account == null)
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player '" + player.getName() + "' does not exist");
        if (account.getBalance() < amount)
            return new EconomyResponse(0, account.getBalance(), EconomyResponse.ResponseType.FAILURE, "Player '" + player.getName() + "' does not have enough money");
        account.setBalance(account.getBalance() - amount);
        account.clean();
        return new EconomyResponse(amount, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        return depositPlayer(offlinePlayer, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        PlayerAccount account = parent.getAccount(player);
        if (account == null)
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player '" + player.getName() + "' does not exist");
        account.setBalance(account.getBalance() + amount);
        account.clean();
        return new EconomyResponse(amount, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        if (!parent.areBanksEnabled())
            return BANK_DISABLED_RESPONSE;
        return createBank(name,Bukkit.getOfflinePlayer(player));
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        if (!parent.areBanksEnabled())
            return BANK_DISABLED_RESPONSE;
        if(parent.getBank(name) != null)return new EconomyResponse(0,0,EconomyResponse.ResponseType.FAILURE, "Bank '" + name + "' already exists");
        if(parent.getBankByOwner(player.getUniqueId())!=null) return new EconomyResponse(0,0, EconomyResponse.ResponseType.FAILURE, "Player already has a bank.");
        parent.createBank(player,name);
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.SUCCESS, null);
    }

    private final EconomyResponse BANK_NOT_FOUND_RESPONSE = new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Could not find a bank with given name.");

    @Override
    public EconomyResponse deleteBank(String name) {
        if (!parent.areBanksEnabled())
            return BANK_DISABLED_RESPONSE;
        BankAccount bank = parent.getBank(name);
        if(bank==null) return BANK_NOT_FOUND_RESPONSE;
        double balance = bank.getBalance();
        bank.delete();
        return new EconomyResponse(0, balance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        if (!parent.areBanksEnabled())
            return BANK_DISABLED_RESPONSE;
        BankAccount byName = parent.getBank(name);
        if(byName==null)return BANK_NOT_FOUND_RESPONSE;
        return new EconomyResponse(0,byName.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        if (!parent.areBanksEnabled())
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are disabled.");
        BankAccount byName = parent.getBank(name);
        if(byName==null)return BANK_NOT_FOUND_RESPONSE;
        if(byName.getBalance()<amount) return new EconomyResponse(amount,byName.getBalance(), EconomyResponse.ResponseType.FAILURE,"Bank '"+name+"' does not have enough money.");
        return new EconomyResponse(amount,byName.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        if (!parent.areBanksEnabled())
            return BANK_DISABLED_RESPONSE;
        BankAccount bank = parent.getBank(name);
        if(bank==null)return BANK_NOT_FOUND_RESPONSE;
        if(bank.getBalance()<amount) return new EconomyResponse(amount,bank.getBalance(), EconomyResponse.ResponseType.FAILURE,"Bank '"+name+"' does not have enough money.");
        bank.setBalance(bank.getBalance()-amount);
        bank.clean();
        return new EconomyResponse(amount,bank.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        if (!parent.areBanksEnabled())
            return BANK_DISABLED_RESPONSE;
        BankAccount bank = parent.getBank(name);
        if(bank==null)return BANK_NOT_FOUND_RESPONSE;
        bank.setBalance(bank.getBalance()+amount);
        bank.clean();
        return new EconomyResponse(amount,bank.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        if (!parent.areBanksEnabled())
            return BANK_DISABLED_RESPONSE;
        return isBankOwner(name,Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        if (!parent.areBanksEnabled())
            return BANK_DISABLED_RESPONSE;
        BankAccount bank = parent.getBank(name);
        if(bank==null) return BANK_NOT_FOUND_RESPONSE;
        if(bank.getOwner().getUniqueId().equals(player.getUniqueId()))return new EconomyResponse(0, bank.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
        else return new EconomyResponse(0, bank.getBalance(), EconomyResponse.ResponseType.FAILURE, player.getName()+" is not the owner of the bank.");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        if (!parent.areBanksEnabled())
            return BANK_DISABLED_RESPONSE;
        return isBankMember(name,Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        if (!parent.areBanksEnabled())
            return BANK_DISABLED_RESPONSE;
        BankAccount bank = parent.getBank(name);
        if(bank==null) return BANK_NOT_FOUND_RESPONSE;
        if(bank.getOwner().getUniqueId().equals(player.getUniqueId())||bank.getMembers().stream().anyMatch(p->p.getUniqueId().equals(player.getUniqueId())))
            return new EconomyResponse(0, bank.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
        return new EconomyResponse(0, bank.getBalance(), EconomyResponse.ResponseType.FAILURE, player.getName() + " is not a member of the bank.");
    }

    @Override
    public List<String> getBanks() {
        if (!parent.areBanksEnabled()) return Collections.emptyList();
        return parent.getAllBanks().stream().map(BankAccount::getName).toList();
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

    public void setBalance(OfflinePlayer target, Double amount) {
        PlayerAccount account = parent.getAccount(target);
        account.setBalance(amount);
        account.clean();
    }

    public Double getDefaultBalance() {
        return parent.getDefaultBalance();
    }

}