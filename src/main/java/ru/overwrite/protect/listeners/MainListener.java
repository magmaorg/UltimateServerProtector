package ru.overwrite.protect.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import ru.overwrite.protect.ServerProtectorManager;
import ru.overwrite.protect.api.ServerProtectorAPI;

public class MainListener implements Listener {
    private final ServerProtectorAPI api;

    public MainListener(ServerProtectorManager plugin) {
        api = plugin.getPluginAPI();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (api.login.isEmpty()) return;
        api.handleInteraction(e.getPlayer(), e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (api.login.isEmpty()) return;
        api.handleInteraction(e.getPlayer(), e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (api.login.isEmpty()) return;
        api.handleInteraction(e.getPlayer(), e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
        if (api.login.isEmpty()) return;
        api.handleInteraction(e.getPlayer(), e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent e) {
        if (api.login.isEmpty()) return;
        if (!(e.getEntity() instanceof Player)) return;
        api.handleInteraction((Player) e.getEntity(), e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(AsyncTabCompleteEvent e) {
        if (api.login.isEmpty()) return;
        if (!(e.getSender() instanceof Player)) return;
        api.handleInteraction((Player) e.getSender(), e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (api.login.isEmpty()) return;
        if (!(e.getEntity() instanceof Player)) return;
        api.handleInteraction((Player) e.getEntity(), e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
        if (api.login.isEmpty()) return;
        if (!(e.getDamager() instanceof Player)) return;
        api.handleInteraction((Player) e.getDamager(), e);
    }
}
