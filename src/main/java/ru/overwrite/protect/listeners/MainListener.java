package ru.overwrite.protect.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import ru.overwrite.protect.ServerProtectorManager;
import ru.overwrite.protect.api.ServerProtectorAPI;
import ru.overwrite.protect.utils.Config;

public class MainListener implements Listener {
    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    public MainListener(ServerProtectorManager plugin) {
        api = plugin.getPluginAPI();
        pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (api.login.isEmpty()) return;
        if (pluginConfig.blocking_settings_allow_orientation_change
                && hasChangedOrientation(e.getFrom(), e.getTo())) {
            return;
        }
        Player p = e.getPlayer();
        api.handleInteraction(p, e);
    }

    private boolean hasChangedOrientation(Location from, Location to) {
        return from.getPitch() != to.getPitch() || from.getYaw() != to.getYaw();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (api.login.isEmpty()) return;
        Player p = e.getPlayer();
        api.handleInteraction(p, e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (api.login.isEmpty()) return;
        Player p = e.getPlayer();
        api.handleInteraction(p, e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
        if (api.login.isEmpty()) return;
        Player p = e.getPlayer();
        if (pluginConfig.blocking_settings_block_item_drop) {
            api.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent e) {
        if (api.login.isEmpty()) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (pluginConfig.blocking_settings_block_item_pickup) {
            api.handleInteraction(p, e);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(AsyncTabCompleteEvent e) {
        if (api.login.isEmpty()) return;
        if (!(e.getSender() instanceof Player)) return;
        Player p = (Player) e.getSender();
        if (pluginConfig.blocking_settings_block_tab_complete) {
            api.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (api.login.isEmpty()) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (pluginConfig.blocking_settings_block_damage) {
            api.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
        if (api.login.isEmpty()) return;
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        if (pluginConfig.blocking_settings_damaging_entity) {
            api.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (api.login.isEmpty()) return;
        Player p = (Player) e.getPlayer();
        if (pluginConfig.blocking_settings_block_inventory_open) {
            api.handleInteraction(p, e);
        }
    }
}
