package ru.overwrite.protect.utils;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import ru.overwrite.protect.Logger;
import ru.overwrite.protect.ServerProtectorManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Config {
    private final ServerProtectorManager plugin;
    private final Logger logger;

    public Config(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.logger = plugin.getPluginLogger();
    }

    public Set<String> perms;

    public Map<String, String> per_player_passwords;

    public List<String> excluded_players;
    public String[] titles_message, titles_incorrect, titles_correct;
    public String uspmsg_reloaded,
            uspmsg_playernotfound,
            uspmsg_alreadyinconfig,
            uspmsg_playeronly,
            uspmsg_logout,
            uspmsg_notinconfig,
            uspmsg_playeradded,
            uspmsg_playerremoved,
            uspmsg_setpassusage,
            uspmsg_rempassusage,
            uspmsg_usage,
            uspmsg_usage_logout,
            uspmsg_usage_reload,
            uspmsg_usage_setpass,
            uspmsg_usage_rempass,
            msg_message,
            msg_incorrect,
            msg_correct,
            msg_cantbenull,
            msg_playeronly,
            broadcasts_failed,
            broadcasts_passed,
            broadcasts_joined,
            broadcasts_captured;
    public boolean blocking_settings_hide_on_entering, blocking_settings_hide_other_on_entering;

    public void setupPasswords(FileConfiguration dataFile) {
        per_player_passwords = new ConcurrentHashMap<>();
        ConfigurationSection data = dataFile.getConfigurationSection("data");
        boolean shouldSave = false;
        for (String nick : data.getKeys(false))
            per_player_passwords.put(nick, data.getString(nick));
        if (shouldSave) save(plugin.path, dataFile, "data.yml");
    }

    public void loadAdditionalChecks(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection blockingSettings = config.getConfigurationSection("blocking-settings");
        if (!configFile.contains("blocking-settings")) {
            logger.warn("Configuration section blocking-settings not found!");
            configFile.createSection("secure-settings");
            configFile.set("blocking-settings.hide-on-entering", true);
            configFile.set("blocking-settings.hide-other-on-entering", true);
            configFile.set("blocking-settings.allow-orientation-change", false);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section blocking-settings");
        }
        blocking_settings_hide_on_entering = blockingSettings.getBoolean("hide-on-entering", true);
        blocking_settings_hide_other_on_entering =
                blockingSettings.getBoolean("hide-other-on-entering", true);
    }

    public void loadPerms(FileConfiguration config) {
        perms = new HashSet<>(config.getStringList("permissions"));
    }

    public void setupExcluded(FileConfiguration config) {
        excluded_players = new ArrayList<>(config.getStringList("excluded-players"));
    }

    public void loadUspMessages(FileConfiguration message) {
        ConfigurationSection uspmsg = message.getConfigurationSection("uspmsg");
        uspmsg_playeronly = getMessage(uspmsg, "playeronly");
        uspmsg_logout = getMessage(uspmsg, "logout");
        uspmsg_reloaded = getMessage(uspmsg, "reloaded");
        uspmsg_playernotfound = getMessage(uspmsg, "playernotfound");
        uspmsg_alreadyinconfig = getMessage(uspmsg, "alreadyinconfig");
        uspmsg_notinconfig = getMessage(uspmsg, "notinconfig");
        uspmsg_playeradded = getMessage(uspmsg, "playeradded");
        uspmsg_playerremoved = getMessage(uspmsg, "playerremoved");
        uspmsg_setpassusage = getMessage(uspmsg, "setpassusage");
        uspmsg_rempassusage = getMessage(uspmsg, "rempassusage");
        uspmsg_usage = getMessage(uspmsg, "usage");
        uspmsg_usage_logout = getMessage(uspmsg, "usage-logout");
        uspmsg_usage_reload = getMessage(uspmsg, "usage-reload");
        uspmsg_usage_setpass = getMessage(uspmsg, "usage-setpass");
        uspmsg_usage_rempass = getMessage(uspmsg, "usage-rempass");
    }

    public void loadMsgMessages(FileConfiguration message) {
        ConfigurationSection msg = message.getConfigurationSection("msg");
        msg_message = getMessage(msg, "message");
        msg_incorrect = getMessage(msg, "incorrect");
        msg_correct = getMessage(msg, "correct");
        msg_cantbenull = getMessage(msg, "cantbenull");
        msg_playeronly = getMessage(msg, "playeronly");
    }

    public void loadTitleMessages(FileConfiguration message) {
        ConfigurationSection titles = message.getConfigurationSection("titles");
        titles_message = getMessage(titles, "message").split(";");
        titles_incorrect = getMessage(titles, "incorrect").split(";");
        titles_correct = getMessage(titles, "correct").split(";");
    }

    public void loadBroadcastMessages(FileConfiguration message) {
        ConfigurationSection broadcasts = message.getConfigurationSection("broadcasts");
        broadcasts_failed = getMessage(broadcasts, "failed");
        broadcasts_passed = getMessage(broadcasts, "passed");
        broadcasts_joined = getMessage(broadcasts, "joined");
        broadcasts_captured = getMessage(broadcasts, "captured");
    }

    public String getMessage(ConfigurationSection section, String key) {
        return ChatColor.translateAlternateColorCodes(
                '&', section.getString(key, "&4&lERROR&r: " + key + " does not exist!"));
    }

    public FileConfiguration getFile(String path, String fileName) {
        File file = new File(path, fileName);
        if (!file.exists()) plugin.saveResource(fileName, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    public void save(String path, FileConfiguration config, String fileName) {
        plugin.getRunner()
                .runAsync(
                        () -> {
                            try {
                                config.save(new File(path, fileName));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });
    }
}
