package ru.overwrite.protect.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import ru.overwrite.protect.ServerProtectorManager;
import ru.overwrite.protect.api.ServerProtectorAPI;

public class ChatListener implements Listener {
    private final ServerProtectorAPI api;

    public ChatListener(ServerProtectorManager plugin) {
        api = plugin.getPluginAPI();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent e) {
        if (!api.isCaptured(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!api.isCaptured(e.getPlayer())) return;
        if (cutCommand(e.getMessage()).equals("/pas")) return;
        e.setCancelled(true);
    }

    private String cutCommand(String str) {
        int index = str.indexOf(' ');
        return index == -1 ? str : str.substring(0, index);
    }
}
