package ru.overwrite.protect.utils.logging;

import ru.overwrite.protect.Logger;
import ru.overwrite.protect.ServerProtectorManager;

public class BukkitLogger implements Logger {
    private final ServerProtectorManager plugin;

    public BukkitLogger(ServerProtectorManager plugin) {
        this.plugin = plugin;
    }

    public void info(String msg) {
        plugin.getLogger().info(msg);
    }

    public void warn(String msg) {
        plugin.getLogger().warning(msg);
    }
}
