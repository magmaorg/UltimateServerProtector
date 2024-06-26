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

    public Set<String> perms, blacklisted_perms;

    public Map<String, String> per_player_passwords;

    public List<String> excluded_admin_pass, excluded_blacklisted_perms;
    public String[] titles_message, titles_incorrect, titles_correct;
    public String uspmsg_consoleonly,
            uspmsg_reloaded,
            uspmsg_rebooted,
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
            uspmsg_usage_reboot,
            uspmsg_usage_setpass,
            uspmsg_usage_rempass,
            uspmsg_otherdisabled,
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
            main_settings_enable_admin_commands,
            punish_settings_enable_attempts,
            punish_settings_enable_time,
            punish_settings_enable_rejoin,
            secure_settings_enable_notadmin_punish,
            secure_settings_enable_permission_blacklist,
            secure_settings_only_console_usp,
            secure_settings_enable_excluded_players,
            secure_settings_call_event_on_password_enter,
            session_settings_session,
            session_settings_session_time_enabled;
    public int punish_settings_max_attempts,
            punish_settings_time,
            punish_settings_max_rejoins,
            session_settings_session_time;

    public long main_settings_check_interval;

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
            configFile.set("main-settings.enable-admin-commands", false);
            configFile.set("main-settings.check-interval", 40);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section main-settings");
        }
        main_settings_prefix = mainSettings.getString("prefix", "[UltimateServerProtector]");
        main_settings_enable_admin_commands =
                mainSettings.getBoolean("enable-admin-commands", false);
        main_settings_check_interval = mainSettings.getLong("check-interval", 40);
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

    public void loadSessionSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection sessionSettings = config.getConfigurationSection("session-settings");
        if (!configFile.contains("session-settings")) {
            logger.warn("Configuration section session-settings not found!");
            configFile.createSection("session-settings");
            configFile.set("session-settings.session", true);
            configFile.set("session-settings.session-time-enabled", true);
            configFile.set("session-settings.session-time", 21600);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section session-settings");
        }
        session_settings_session = sessionSettings.getBoolean("session", true);
        session_settings_session_time_enabled =
                sessionSettings.getBoolean("session-time-enabled", true);
        session_settings_session_time = sessionSettings.getInt("session-time", 21600);
    }

    public void loadPunishSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection punishSettings = config.getConfigurationSection("punish-settings");
        if (!configFile.contains("punish-settings")) {
            logger.warn("Configuration section punish-settings not found!");
            configFile.createSection("punish-settings");
            configFile.set("punish-settings.enable-attempts", true);
            configFile.set("punish-settings.max-attempts", 3);
            configFile.set("punish-settings.enable-time", true);
            configFile.set("punish-settings.time", 60);
            configFile.set("punish-settings.enable-rejoin", true);
            configFile.set("punish-settings.max-rejoins", 3);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section punish-settings");
        }
        punish_settings_enable_attempts = punishSettings.getBoolean("enable-attempts", true);
        punish_settings_max_attempts = punishSettings.getInt("max-attempts", 3);
        punish_settings_enable_time = punishSettings.getBoolean("enable-time", true);
        punish_settings_time = punishSettings.getInt("time", 60);
        punish_settings_enable_rejoin = punishSettings.getBoolean("enable-rejoin", true);
        punish_settings_max_rejoins = punishSettings.getInt("max-rejoins", 3);
    }

    public void loadSecureSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection secureSettings = config.getConfigurationSection("secure-settings");
        if (!configFile.contains("secure-settings")) {
            logger.warn("Configuration section secure-settings not found!");
            configFile.createSection("secure-settings");
            configFile.set("secure-settings.enable-notadmin-punish", false);
            configFile.set("secure-settings.enable-permission-blacklist", false);
            configFile.set("secure-settings.only-console-usp", false);
            configFile.set("secure-settings.enable-excluded-players", false);
            configFile.set("secure-settings.call-event-on-password-enter", false);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section secure-settings");
        }
        secure_settings_enable_notadmin_punish =
                secureSettings.getBoolean("enable-notadmin-punish", false);
        secure_settings_enable_permission_blacklist =
                secureSettings.getBoolean("enable-permission-blacklist", false);
        secure_settings_only_console_usp = secureSettings.getBoolean("only-console-usp", false);
        secure_settings_enable_excluded_players =
                secureSettings.getBoolean("enable-excluded-players", false);
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

    public void loadLists(FileConfiguration config) {
        ConfigurationSection secureSettings = config.getConfigurationSection("secure-settings");
        if (secureSettings.getBoolean("enable-permission-blacklist")) {
            blacklisted_perms = new HashSet<>(config.getStringList("blacklisted-perms"));
        }
    }

    public void setupExcluded(FileConfiguration config) {
        if (config.getBoolean("secure-settings.enable-excluded-players")) {
            ConfigurationSection excludedPlayers =
                    config.getConfigurationSection("excluded-players");
            excluded_admin_pass = new ArrayList<>(excludedPlayers.getStringList("admin-pass"));
            excluded_blacklisted_perms =
                    new ArrayList<>(excludedPlayers.getStringList("blacklisted-perms"));
        }
    }

    public void loadUspMessages(FileConfiguration message) {
        ConfigurationSection uspmsg = message.getConfigurationSection("uspmsg");
        uspmsg_consoleonly = getMessage(uspmsg, "consoleonly");
        uspmsg_playeronly = getMessage(uspmsg, "playeronly");
        uspmsg_logout = getMessage(uspmsg, "logout");
        uspmsg_reloaded = getMessage(uspmsg, "reloaded");
        uspmsg_rebooted = getMessage(uspmsg, "rebooted");
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
        uspmsg_usage_reboot = getMessage(uspmsg, "usage-reboot");
        uspmsg_usage_setpass = getMessage(uspmsg, "usage-setpass");
        uspmsg_usage_rempass = getMessage(uspmsg, "usage-rempass");
        uspmsg_otherdisabled = getMessage(uspmsg, "otherdisabled");
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
