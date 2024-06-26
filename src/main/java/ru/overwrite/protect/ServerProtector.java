package ru.overwrite.protect;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.Date;

public final class ServerProtector extends ServerProtectorManager {
    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        setupLogger(config);
        loadConfigs(config);
        PluginManager pluginManager = server.getPluginManager();
        registerListeners(pluginManager);
        registerCommands(pluginManager, config);
        startTasks(config);
        logEnableDisable(messageFile.getString("log-format.enabled"), new Date(startTime));
        long endTime = System.currentTimeMillis();
        getPluginLogger().info("Plugin started in " + (endTime - startTime) + " ms");
    }

    @Override
    public void onDisable() {
        if (messageFile != null)
            logEnableDisable(messageFile.getString("log-format.disabled"), new Date());
        for (Player ps : server.getOnlinePlayers())
            if (ps.hasPermission("serverprotector.admin") && messageFile != null)
                ps.sendMessage(
                        getPluginConfig()
                                .getMessage(
                                        messageFile.getConfigurationSection("broadcasts"),
                                        "disabled"));
        getRunner().cancelTasks();
    }
}
