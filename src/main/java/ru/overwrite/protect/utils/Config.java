package ru.overwrite.protect.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

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
            msg_noneed,
            msg_cantbenull,
            msg_playeronly,
            broadcasts_failed,
            broadcasts_passed,
            broadcasts_joined,
            broadcasts_captured,
            bossbar_message,
            main_settings_prefix;
    public boolean blocking_settings_block_item_drop,
            blocking_settings_block_item_pickup,
            blocking_settings_block_tab_complete,
            blocking_settings_block_damage,
            blocking_settings_damaging_entity,
            blocking_settings_block_inventory_open,
            blocking_settings_hide_on_entering,
            blocking_settings_hide_other_on_entering,
            blocking_settings_allow_orientation_change,
            secure_settings_enable_notadmin_punish,
            secure_settings_call_event_on_password_enter;

    public void setupPasswords(FileConfiguration dataFile) {
        per_player_passwords = new ConcurrentHashMap<>();
        ConfigurationSection data = dataFile.getConfigurationSection("data");
        boolean shouldSave = false;
        for (String nick : data.getKeys(false)) {
            per_player_passwords.put(nick, data.getString(nick + ".pass"));
        }
        if (shouldSave) {
            save(plugin.path, dataFile, "data.yml");
        }
    }

    public void loadMainSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection mainSettings = config.getConfigurationSection("main-settings");
        if (!configFile.contains("main-settings")) {
            logger.warn("Configuration section main-settings not found!");
            configFile.createSection("main-settings");
            configFile.set("main-settings.prefix", "[UltimateServerProtector]");
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section main-settings");
        }
        main_settings_prefix = mainSettings.getString("prefix", "[UltimateServerProtector]");
    }

    public void loadAdditionalChecks(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection blockingSettings = config.getConfigurationSection("blocking-settings");
        if (!configFile.contains("blocking-settings")) {
            logger.warn("Configuration section blocking-settings not found!");
            configFile.createSection("secure-settings");
            configFile.set("blocking-settings.block-item-drop", true);
            configFile.set("blocking-settings.block-item-pickup", true);
            configFile.set("blocking-settings.block-tab-complete", true);
            configFile.set("blocking-settings.block-damage", true);
            configFile.set("blocking-settings.block-damaging-entity", true);
            configFile.set("blocking-settings.block-inventory-open", false);
            configFile.set("blocking-settings.hide-on-entering", true);
            configFile.set("blocking-settings.hide-other-on-entering", true);
            configFile.set("blocking-settings.allow-orientation-change", false);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section blocking-settings");
        }
        blocking_settings_block_item_drop = blockingSettings.getBoolean("block-item-drop", true);
        blocking_settings_block_item_pickup =
                blockingSettings.getBoolean("block-item-pickup", true);
        blocking_settings_block_tab_complete =
                blockingSettings.getBoolean("block-tab-complete", true);
        blocking_settings_block_damage = blockingSettings.getBoolean("block-damage", true);
        blocking_settings_damaging_entity =
                blockingSettings.getBoolean("block-damaging-entity", true);
        blocking_settings_block_inventory_open =
                blockingSettings.getBoolean("block-inventory-open", false);
        blocking_settings_hide_on_entering = blockingSettings.getBoolean("hide-on-entering", true);
        blocking_settings_hide_other_on_entering =
                blockingSettings.getBoolean("hide-other-on-entering", true);
        blocking_settings_allow_orientation_change =
                blockingSettings.getBoolean("allow-orientation-change", false);
    }

    public void loadSecureSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection secureSettings = config.getConfigurationSection("secure-settings");
        if (!configFile.contains("secure-settings")) {
            logger.warn("Configuration section secure-settings not found!");
            configFile.createSection("secure-settings");
            configFile.set("secure-settings.enable-notadmin-punish", false);
            configFile.set("secure-settings.call-event-on-password-enter", false);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section secure-settings");
        }
        secure_settings_enable_notadmin_punish =
                secureSettings.getBoolean("enable-notadmin-punish", false);
        secure_settings_call_event_on_password_enter =
                secureSettings.getBoolean("call-event-on-password-enter", false);
    }

    public void loadBossbarSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection bossbar = plugin.messageFile.getConfigurationSection("bossbar");
        bossbar_message = getMessage(bossbar, "message");
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
        msg_noneed = getMessage(msg, "noneed");
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
        Component component =
                MiniMessage.miniMessage()
                        .deserialize(
                                section.getString(key, "&4&lERROR&r: " + key + " does not exist!")
                                        .replace("%prefix%", main_settings_prefix));
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public FileConfiguration getFile(String path, String fileName) {
        File file = new File(path, fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
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
