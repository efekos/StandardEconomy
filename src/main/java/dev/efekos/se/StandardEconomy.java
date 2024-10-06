package dev.efekos.se;

import dev.efekos.se.config.Config;
import dev.efekos.se.impl.EconomyProvider;
import dev.efekos.simple_ql.SimpleQL;
import dev.efekos.simple_ql.data.Database;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class StandardEconomy extends JavaPlugin {

    private Config config;
    private Database database;
    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        setupConfig();
        setupDatabase();
        if (setupEconomy()) return;

        long ms = System.currentTimeMillis() - start;
        getLogger().info("Enabled StandardEconomy (took "+ms+"ms)");
    }

    private boolean setupEconomy() {
        if(foundVault()){
            getLogger().info("Found Vault. Hooking...");
            getServer().getServicesManager().register(Economy.class,new EconomyProvider(this),this, ServicePriority.Highest);
            getLogger().info("Hooked into Vault.");
        } else {
            getLogger().severe("Cannot find Vault! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return true;
        }
        return false;
    }

    private boolean foundVault(){
        PluginManager plm = getServer().getPluginManager();
        return plm.getPlugin("Vault") != null || plm.isPluginEnabled("Vault");
    }

    private void setupDatabase() {
        boolean useMySql = config.getBoolean("database.enable-external-databases", false);
        String url = useMySql ? "jdbc://mysql:" + config.getString("database.host", "localhost") + ":" + config.getString("database.port", "3306") : "jdbc://sqlite:" + Path.of(getDataPath().toString(), ".database");
        database = useMySql ? SimpleQL.createDatabase(url,"dev_efekos_se",config.getString("database.username","admin"),config.getString("database.password","admin")) : SimpleQL.createDatabase(url);
    }

    private void setupConfig() {
        config = new Config("config.yml", this);
        config.setup();
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

    public int getFractionalDigits(){
        return config.getInt("digits",0);
    }

}
