package ru.overwrite.protect.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ru.overwrite.protect.ServerProtectorManager;
import ru.overwrite.protect.api.ServerProtectorAPI;

public class TabCompleteListener implements Listener {
    private final ServerProtectorAPI api;

    public TabCompleteListener(ServerProtectorManager plugin) {
        api = plugin.getPluginAPI();
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(AsyncTabCompleteEvent e) {
        if (api.login.isEmpty()) return;
        if (!(e.getSender() instanceof Player)) return;
        api.handleInteraction((Player) e.getSender(), e);
    }
}
