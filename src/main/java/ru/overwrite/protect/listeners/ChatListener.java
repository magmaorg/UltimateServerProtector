package ru.overwrite.protect.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import ru.overwrite.protect.PasswordHandler;
import ru.overwrite.protect.ServerProtectorManager;
import ru.overwrite.protect.api.ServerProtectorAPI;
import ru.overwrite.protect.utils.Config;

public class ChatListener implements Listener {
    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final PasswordHandler passwordHandler;
    private final Config pluginConfig;

    public ChatListener(ServerProtectorManager plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
        passwordHandler = plugin.getPasswordHandler();
        api = plugin.getPluginAPI();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!api.isCaptured(p)) {
            return;
        }
        e.setCancelled(true);
        e.setMessage("");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!api.isCaptured(p)) {
            return;
        }
        String message = e.getMessage();
        String label = cutCommand(message).toLowerCase();
        if (label.equals("/" + pluginConfig.main_settings_pas_command)) {
            if (!plugin.paper) {
                passwordHandler.checkPassword(p, message.split(" ")[1], false);
            }
            return;
        }
        for (String command : pluginConfig.allowed_commands) {
            if (label.equals(command) || message.equalsIgnoreCase(command)) {
                return;
            }
        }
        e.setCancelled(true);
    }

    private String cutCommand(String str) {
        int index = str.indexOf(' ');
        return index == -1 ? str : str.substring(0, index);
    }
}
