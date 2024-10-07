package dev.efekos.se;

import dev.efekos.se.commands.BalanceCommand;
import dev.efekos.se.commands.EconomyCommand;
import dev.efekos.se.commands.PayCommand;
import dev.efekos.se.config.Config;
import dev.efekos.se.data.PlayerAccount;
import dev.efekos.se.impl.EconomyProvider;
import dev.efekos.simple_ql.SimpleQL;
import dev.efekos.simple_ql.data.Database;
import dev.efekos.simple_ql.data.Table;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.List;

public final class StandardEconomy extends JavaPlugin {

    private static EconomyProvider provider;
    private Config config;
    private Table<PlayerAccount> accounts;

    public static EconomyProvider getProvider() {
        return provider;
    }

    public static Component format(String message, TagResolver... components) {
        return MiniMessage.builder().build().deserialize(message, components);
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        setupConfig();
        setupDatabase();
        if (setupEconomy()) return;
        setupCommands();

        long ms = System.currentTimeMillis() - start;
        getLogger().info("Enabled StandardEconomy (took " + ms + "ms)");
    }

    private void setupCommands() {
        LifecycleEventManager<Plugin> manager = getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            Commands registrar = e.registrar();
            List.of(new EconomyCommand(), new BalanceCommand(), new PayCommand()).forEach(c -> c.register(registrar));
        });
    }

    private boolean setupEconomy() {
        if (foundVault()) {
            getLogger().info("Found Vault. Hooking...");
            provider = new EconomyProvider(this);
            getServer().getServicesManager().register(Economy.class, provider, this, ServicePriority.Highest);
            getLogger().info("Hooked into Vault.");
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

    public int getFractionalDigits() {
        return config.getInt("digits", 0);
    }

    public PlayerAccount getAccount(OfflinePlayer player) {
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
