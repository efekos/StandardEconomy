package dev.efekos.se;

import dev.efekos.se.commands.BalanceCommand;
import dev.efekos.se.commands.EconomyCommand;
import dev.efekos.se.config.Config;
import dev.efekos.se.data.PlayerAccount;
import dev.efekos.se.impl.EconomyProvider;
import dev.efekos.simple_ql.SimpleQL;
import dev.efekos.simple_ql.data.Database;
import dev.efekos.simple_ql.data.Table;
import me.lucko.commodore.Commodore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import me.lucko.commodore.CommodoreProvider;

import java.nio.file.Path;

public final class StandardEconomy extends JavaPlugin {

    private Config config;
    private Table<PlayerAccount> accounts;
    private static EconomyProvider provider;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        setupConfig();
        setupDatabase();
        if (setupEconomy()) return;
        setupCommands();

        long ms = System.currentTimeMillis() - start;
        getLogger().info("Enabled StandardEconomy (took "+ms+"ms)");
    }

    private void setupCommands() {
        getServer().getCommandMap().register("balance","se",new BalanceCommand("balance"));
        getServer().getCommandMap().register("bal","se",new BalanceCommand("bal"));
        getServer().getCommandMap().register("money","se",new BalanceCommand("money"));

        if(CommodoreProvider.isSupported()){
            Commodore commodore = CommodoreProvider.getCommodore(this);
            new EconomyCommand().register(commodore);
        }
    }

    public static EconomyProvider getProvider() {
        return provider;
    }

    private boolean setupEconomy() {
        if(foundVault()){
            getLogger().info("Found Vault. Hooking...");
            provider = new EconomyProvider(this);
            getServer().getServicesManager().register(Economy.class,provider,this, ServicePriority.Highest);
            getLogger().info("Hooked into Vault.");
            return false;
        } else {
            getLogger().severe("Cannot find Vault! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return true;
        }
    }
    private boolean foundVault(){
        PluginManager plm = getServer().getPluginManager();
        return plm.getPlugin("Vault") != null || plm.isPluginEnabled("Vault");
    }
    private void setupDatabase() {
        boolean useMySql = config.getBoolean("database.enable-external-databases", false);
        String url = useMySql ? "jdbc:mysql:" + config.getString("database.host", "localhost") + ":" + config.getString("database.port", "3306") : "jdbc:sqlite:" + Path.of(getDataFolder().getAbsolutePath(), "database");
        Database database = useMySql ? SimpleQL.createDatabase(url, "dev_efekos_se", config.getString("database.username", "admin"), config.getString("database.password", "admin")) : SimpleQL.createDatabase(url);
        accounts = database.registerTable("accounts",PlayerAccount.class);
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

    public PlayerAccount getAccount(OfflinePlayer player){
        return accounts.getRow(player.getUniqueId()).orElseGet(() -> accounts.insertRow(playerAccount -> {
            playerAccount.setId(player.getUniqueId());
            playerAccount.setBalance(0);
            playerAccount.setName(player.getName());
        }));
    }

    public PlayerAccount getAccount(String name) {
        return getAccount(Bukkit.getOfflinePlayer(name));
    }

}
