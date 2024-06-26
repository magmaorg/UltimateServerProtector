package ru.overwrite.protect;

import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.overwrite.protect.api.CaptureReason;
import ru.overwrite.protect.api.ServerProtectorAPI;
import ru.overwrite.protect.commands.PasCommand;
import ru.overwrite.protect.commands.UspCommand;
import ru.overwrite.protect.listeners.ChatListener;
import ru.overwrite.protect.listeners.ConnectionListener;
import ru.overwrite.protect.listeners.MainListener;
import ru.overwrite.protect.listeners.TabCompleteListener;
import ru.overwrite.protect.task.BukkitRunner;
import ru.overwrite.protect.task.Runner;
import ru.overwrite.protect.task.TaskManager;
import ru.overwrite.protect.utils.Config;
import ru.overwrite.protect.utils.Utils;
import ru.overwrite.protect.utils.logging.BukkitLogger;

import java.io.*;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerProtectorManager extends JavaPlugin {
    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
    private final Logger pluginLogger = new BukkitLogger(this);

    public FileConfiguration messageFile;
    public FileConfiguration dataFile;

    public String path;

    private final Config pluginConfig = new Config(this);
    private final ServerProtectorAPI api = new ServerProtectorAPI(this);
    private final PasswordHandler passwordHandler = new PasswordHandler(this);
    private final Runner runner = new BukkitRunner(this);

    public Map<String, Integer> time;

    private File logFile;

    public final Server server = getServer();

    public Config getPluginConfig() {
        return pluginConfig;
    }

    public ServerProtectorAPI getPluginAPI() {
        return api;
    }

    public PasswordHandler getPasswordHandler() {
        return passwordHandler;
    }

    public Logger getPluginLogger() {
        return pluginLogger;
    }

    public Runner getRunner() {
        return runner;
    }

    public void loadConfigs(FileConfiguration config) {
        path = getDataFolder().getAbsolutePath();
        dataFile = pluginConfig.getFile(path, "data.yml");
        pluginConfig.save(path, dataFile, "data.yml");
        messageFile = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
        pluginConfig.save(getDataFolder().getAbsolutePath(), messageFile, "message.yml");
        setupPluginConfig(config);
        pluginConfig.setupPasswords(dataFile);
    }

    public void reloadConfigs() {
        runner.runAsync(
                () -> {
                    reloadConfig();
                    FileConfiguration config = getConfig();
                    messageFile =
                            pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
                    path = getDataFolder().getAbsolutePath();
                    dataFile = pluginConfig.getFile(path, "data.yml");
                    setupPluginConfig(config);
                    pluginConfig.setupPasswords(dataFile);
                });
    }

    private void setupPluginConfig(FileConfiguration config) {
        pluginConfig.loadPerms(config);
        pluginConfig.loadLists(config);
        pluginConfig.setupExcluded(config);
        FileConfiguration configFile = pluginConfig.getFile(path, "config.yml");
        pluginConfig.loadMainSettings(config, configFile);
        pluginConfig.loadSecureSettings(config, configFile);
        pluginConfig.loadAdditionalChecks(config, configFile);
        pluginConfig.loadBossbarSettings(config, configFile);
        pluginConfig.loadMsgMessages(messageFile);
        pluginConfig.loadUspMessages(messageFile);
        pluginConfig.loadTitleMessages(messageFile);
        pluginConfig.loadBroadcastMessages(messageFile);
    }

    public void registerListeners(PluginManager pluginManager) {
        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new ConnectionListener(this), this);
        pluginManager.registerEvents(new MainListener(this), this);
        if (pluginConfig.blocking_settings_block_tab_complete) {
            pluginManager.registerEvents(new TabCompleteListener(this), this);
        }
    }

    public void registerCommands(PluginManager pluginManager, FileConfiguration config) {
        try {
            CommandMap commandMap = server.getCommandMap();
            Constructor<PluginCommand> constructor =
                    PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand command = constructor.newInstance("pas", this);
            command.setExecutor(new PasCommand(this));
            commandMap.register(getDescription().getName(), command);
        } catch (Exception e) {
            pluginLogger.info("Unable to register password command!");
            e.printStackTrace();
            pluginManager.disablePlugin(this);
        }
        PluginCommand uspCommand = getCommand("ultimateserverprotector");
        UspCommand uspCommandClass = new UspCommand(this);
        uspCommand.setExecutor(uspCommandClass);
        uspCommand.setTabCompleter(uspCommandClass);
    }

    public void startTasks(FileConfiguration config) {
        TaskManager taskManager = new TaskManager(this);
        taskManager.startMainCheck(pluginConfig.main_settings_check_interval);
        taskManager.startCapturesMessages(config);
        time = new ConcurrentHashMap<>();
        taskManager.startCapturesTimer(config);
        if (pluginConfig.secure_settings_enable_notadmin_punish) {
            taskManager.startAdminCheck(config);
        }
        if (pluginConfig.secure_settings_enable_permission_blacklist) {
            taskManager.startPermsCheck(config);
        }
    }

    public void setupLogger(FileConfiguration config) {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new RuntimeException("Unable to create data folder");
        }
        String logFilePath = dataFolder.getPath();
        logFile = new File(logFilePath, "log.yml");
    }

    public void checkFail(String playerName, List<String> command) {
        if (command.isEmpty()) {
            return;
        }
        runner.run(
                () -> {
                    for (String c : command) {
                        server.dispatchCommand(
                                server.getConsoleSender(), c.replace("%player%", playerName));
                        Date date = new Date();
                        logToFile(
                                messageFile
                                        .getString("log-format.command", "ERROR")
                                        .replace("%player%", playerName)
                                        .replace("%cmd%", c)
                                        .replace("%date%", DATE_FORMAT.format(date)));
                    }
                });
    }

    public void giveEffect(Player player) {
        runner.runPlayer(
                () -> {
                    PotionEffectType types = PotionEffectType.getByName("BLINDNESS");
                    player.addPotionEffect(new PotionEffect(types, 99999, 2));
                },
                player);
    }

    public void applyHide(Player p) {
        if (pluginConfig.blocking_settings_hide_on_entering) {
            runner.runPlayer(
                    () -> {
                        for (Player onlinePlayer : server.getOnlinePlayers()) {
                            if (!onlinePlayer.equals(p)) {
                                onlinePlayer.hidePlayer(this, p);
                            }
                        }
                    },
                    p);
        }
        if (pluginConfig.blocking_settings_hide_other_on_entering) {
            runner.runPlayer(
                    () -> {
                        for (Player onlinePlayer : server.getOnlinePlayers()) {
                            p.hidePlayer(this, onlinePlayer);
                        }
                    },
                    p);
        }
    }

    public void logEnableDisable(String msg, Date date) {
        logToFile(msg.replace("%date%", DATE_FORMAT.format(date)));
    }

    public CaptureReason checkPermissions(Player p) {
        if (p.isOp()) {
            return new CaptureReason(null);
        }
        if (p.hasPermission("serverprotector.protect")) {
            return new CaptureReason("serverprotector.protect");
        }
        for (String s : pluginConfig.perms) {
            if (p.hasPermission(s)) {
                return new CaptureReason(s);
            }
        }
        return null;
    }

    public boolean isExcluded(Player p, List<String> list) {
        return pluginConfig.secure_settings_enable_excluded_players && list.contains(p.getName());
    }

    public boolean isAdmin(String nick) {
        return pluginConfig.per_player_passwords.containsKey(nick);
    }

    public void sendAlert(Player p, String msg) {
        msg = msg.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p));
        for (Player ps : server.getOnlinePlayers()) {
            if (ps.hasPermission("serverprotector.admin")) {
                ps.sendMessage(msg);
            }
        }
        server.getConsoleSender()
                .sendMessage(msg.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p)));
    }

    public void logAction(String key, Player player, Date date) {
        runner.runAsync(
                () ->
                        logToFile(
                                messageFile
                                        .getString(key, "ERROR: " + key + " does not exist!")
                                        .replace("%player%", player.getName())
                                        .replace("%ip%", Utils.getIp(player))
                                        .replace("%date%", DATE_FORMAT.format(date))));
    }

    public void logToFile(String message) {
        try {
            FileWriter fileWriter = new FileWriter(logFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
