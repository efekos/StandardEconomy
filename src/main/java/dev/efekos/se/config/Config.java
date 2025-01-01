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

package dev.efekos.se.config;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class Config {

    private final String resourceName;
    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration fileConfiguration;

    public Config(String resourceName, JavaPlugin plugin) {
        this.resourceName = resourceName.endsWith(".yml") ? resourceName : resourceName + ".yml";
        this.plugin = plugin;
    }

    public void setup() {
        file = new File(plugin.getDataFolder(), resourceName);

        if (!file.exists()) {
            try {
                plugin.saveResource(resourceName, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration get() {
        return fileConfiguration;
    }

    public String getString(String path, String def) {
        return fileConfiguration.getString(path, def);
    }

    public int getInt(String path, Integer def) {
        return fileConfiguration.getInt(path, def);
    }

    public boolean getBoolean(String path, Boolean def) {
        return fileConfiguration.getBoolean(path, def);
    }

    public List<String> getStringList(String path) {
        return fileConfiguration.getStringList(path);
    }

    public Location getLocation(String path, Location def) {
        return fileConfiguration.getLocation(path, def);
    }

    public Location getLocation(String path) {
        return fileConfiguration.getLocation(path);
    }

    public double getDouble(String path, double def) {
        return fileConfiguration.getDouble(path, def);
    }

    public void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }
}

