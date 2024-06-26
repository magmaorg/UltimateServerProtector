package ru.overwrite.protect.utils.logging;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import ru.overwrite.protect.Logger;
import ru.overwrite.protect.ServerProtectorManager;

public class PaperLogger implements Logger {
    private final ServerProtectorManager plugin;

    private final LegacyComponentSerializer legacySection =
            LegacyComponentSerializer.legacySection();

    public PaperLogger(ServerProtectorManager plugin) {
        this.plugin = plugin;
    }

    public void info(String msg) {
        plugin.getComponentLogger().info(legacySection.deserialize(msg));
    }

    public void warn(String msg) {
        plugin.getComponentLogger().warn(legacySection.deserialize(msg));
    }
}
