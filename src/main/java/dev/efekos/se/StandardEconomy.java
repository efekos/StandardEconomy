/*
 * MIT License
 *
 * Copyright (c) 2024 efekos
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

package dev.efekos.se;

import dev.efekos.arn.common.Arn;
import dev.efekos.arn.common.ArnInstance;
import dev.efekos.arn.common.exception.ArnException;
import dev.efekos.se.commands.*;
import dev.efekos.se.config.Config;
import dev.efekos.se.data.BankAccount;
import dev.efekos.se.data.PlayerAccount;
import dev.efekos.se.impl.EconomyProvider;
import dev.efekos.simple_ql.SimpleQL;
import dev.efekos.simple_ql.data.Database;
import dev.efekos.simple_ql.data.Table;
import dev.efekos.simple_ql.query.Conditions;
import dev.efekos.simple_ql.query.QueryBuilder;
import dev.efekos.simple_ql.query.QueryResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class StandardEconomy extends JavaPlugin {

    private static EconomyProvider provider;
    private Config config;
    private static Config language;
    private Table<PlayerAccount> accounts;
    private Table<BankAccount> banks;

    public static EconomyProvider getProvider() {
        return provider;
    }

    public static Component format(String message, TagResolver... components) {
        return MiniMessage.builder().build().deserialize(language.getString(message,"<red>"+message+"</red>"), components);
    }

    public static String getKey(String key){
        return language.getString(key,key);
    }

    private Metrics metrics;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        setupConfig();
        setupDatabase();
        if (setupEconomy()) return;
        setupCommands();
        setupMetrics();

        long ms = System.currentTimeMillis() - start;
        getLogger().info("Enabled StandardEconomy (took " + ms + "ms)");
    }

    private void setupMetrics() {
        metrics = new Metrics(this,23608);
        metrics.addCustomChart(new SimplePie("bankUsage",() -> areBanksEnabled()?"Uses banks":"Doesn't use banks"));
        metrics.addCustomChart(new SingleLineChart("banks",() -> getAllBanks().size()));
    }

    private void setupCommands() {
        ArnInstance arn = Arn.getInstance();
        if(!areBanksEnabled())arn.excludeClass(BankCommand.class);
        try {
            arn.run(StandardEconomy.class,this);
        } catch (ArnException e) {
            e.printStackTrace();
            getLogger().severe("Could not register commands!");
        }

    }

    private boolean setupEconomy() {
        if (foundVault()) {
            getLogger().info("Found Vault. Hooking...");
            provider = new EconomyProvider(this);
            getServer().getServicesManager().register(Economy.class, provider, this, ServicePriority.Highest);
            getLogger().info("Hooked into Vault.");

            long i = config.getInt("clear-cache-interval", 10) * 60 * 20L;
            new BukkitRunnable(){
                @Override
                public void run() {
                    provider.clearBalTopCache();
                    getLogger().info("Cleared /baltop cache.");
                }
            }.runTaskTimer(this, i,i);
            getLogger().info("Created /baltop cache clear task.");

            return false;
        } else {
            getLogger().severe("Cannot find Vault! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return true;
        }
    }

    private boolean foundVault() {
        PluginManager plm = getServer().getPluginManager();
        return plm.getPlugin("Vault") != null || plm.isPluginEnabled("Vault");
    }

    private void setupDatabase() {
        boolean useMySql = config.getBoolean("database.enable-external-databases", false);
        String url = useMySql ? "jdbc:mysql:" + config.getString("database.host", "localhost") + ":" + config.getString("database.port", "3306") : "jdbc:sqlite:" + Path.of(getDataFolder().getAbsolutePath(), "database");
        Database database = useMySql ? SimpleQL.createDatabase(url, "dev_efekos_se", config.getString("database.username", "admin"), config.getString("database.password", "admin")) : SimpleQL.createDatabase(url);
        accounts = database.registerTable("accounts", PlayerAccount.class);
        if(areBanksEnabled()) banks = database.registerTable("banks",BankAccount.class);
    }

    private void setupConfig() {
        config = new Config("config.yml", this);
        config.setup();
        language = new Config("lang.yml",this);
        language.setup();
    }

    public boolean areBanksEnabled() {
        return config.getBoolean("enable-banks", false);
    }

    public String getCurrencySymbol() {
        return config.getString("currency.symbol", "$");
    }

    public String getCurrencyName(boolean plural) {
        return config.getString("currency." + (plural ? "plural" : "single"), plural ? "dollars" : "dollar");
    }

    public double getDefaultBalance(){
        return config.getDouble("default-balance",0d);
    }

    public int getFractionalDigits() {
        return config.getInt("digits", 0);
    }

    public PlayerAccount getAccount(OfflinePlayer player) {
        return accounts.getRow(player.getUniqueId()).orElseGet(() -> accounts.insertRow(playerAccount -> {
            playerAccount.setId(player.getUniqueId());
            playerAccount.setBalance(getDefaultBalance());
            playerAccount.setName(player.getName());
        }));
    }

    public PlayerAccount getAccount(String name) {
        return getAccount(Bukkit.getOfflinePlayer(name));
    }

    public BankAccount getBank(String name) {
        if(!areBanksEnabled())return null;
        return banks.getRow(name).orElse(null);
    }

    public BankAccount getBankByOwner(UUID id) {
        if(!areBanksEnabled())return null;
        QueryResult<BankAccount> result = banks.query(new QueryBuilder().limit(1).filterWithCondition(Conditions.matchTextExact("owner", id.toString())).getQuery());
        if(result.hasResult()) return result.result().isEmpty()?null:result.result().getFirst();
        return null;
    }

    public List<BankAccount> getAllBanks(){
        if(!areBanksEnabled())return new ArrayList<>();
        QueryResult<BankAccount> result = banks.query(new QueryBuilder().getQuery());
        if(result.hasResult())return result.result();
        return new ArrayList<>();
    }

    public void createBank(OfflinePlayer owner,String name){
        banks.insertRow(bank -> {
            bank.setOwner(owner);
            bank.setName(name);
            bank.setBalance(0);
        });
    }

    public List<PlayerAccount> getTopTen(int page){
        QueryBuilder builder = new QueryBuilder();
        if(page!=0)builder.skip(page * 10);
        QueryResult<PlayerAccount> result = accounts.query(builder.limit(10).sortDescending("balance").getQuery());
        return Optional.ofNullable(result.result()).orElse(new ArrayList<>());
    }

}
