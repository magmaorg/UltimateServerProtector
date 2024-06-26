package ru.overwrite.protect.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;

import ru.overwrite.protect.ServerProtectorManager;
import ru.overwrite.protect.api.CaptureReason;
import ru.overwrite.protect.api.ServerProtectorAPI;
import ru.overwrite.protect.api.ServerProtectorCaptureEvent;
import ru.overwrite.protect.task.Runner;
import ru.overwrite.protect.utils.Config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ConnectionListener implements Listener {
    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    private final Runner runner;

    public ConnectionListener(ServerProtectorManager plugin) {
        this.plugin = plugin;
        api = plugin.getPluginAPI();
        pluginConfig = plugin.getPluginConfig();
        runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        runner.runAsync(
                () -> {
                    CaptureReason captureReason = plugin.checkPermissions(p);
                    if (api.isCaptured(p) && captureReason == null) {
                        api.uncapturePlayer(p);
                        return;
                    }
                    if (captureReason != null) {
                        String playerName = p.getName();
                        String ip = e.getAddress().getHostAddress();
                        if (!api.ips.contains(playerName + ip)) {
                            if (!plugin.isExcluded(p, pluginConfig.excluded_players)) {
                                ServerProtectorCaptureEvent captureEvent =
                                        new ServerProtectorCaptureEvent(p, ip, captureReason);
                                captureEvent.callEvent();
                                if (captureEvent.isCancelled()) {
                                    return;
                                }
                                api.capturePlayer(p);
                            }
                        }
                    }
                });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        runner.runAsync(
                () -> {
                    CaptureReason captureReason = plugin.checkPermissions(p);
                    if (captureReason != null) {
                        if (api.isCaptured(p)) {
                            plugin.giveEffect(p);
                            plugin.applyHide(p);
                        }
                        plugin.logAction("log-format.joined", p, new Date());
                        plugin.sendAlert(p, pluginConfig.broadcasts_joined);
                    }
                });
    }

    public final Map<String, Integer> rejoins = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        handlePlayerLeave(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        Player p = event.getPlayer();
        handlePlayerLeave(p);
    }

    private void handlePlayerLeave(Player p) {
        String playerName = p.getName();
        if (api.isCaptured(p)) {
            for (PotionEffect s : p.getActivePotionEffects()) {
                p.removePotionEffect(s.getType());
            }
            rejoins.put(playerName, rejoins.getOrDefault(playerName, 0) + 1);
            if (isMaxRejoins(playerName)) {
                rejoins.remove(playerName);
                plugin.checkFail(
                        p.getName(), plugin.getConfig().getStringList("commands.failed-rejoin"));
            }
        }
        plugin.time.remove(playerName);
        api.saved.remove(playerName);
    }

    private boolean isMaxRejoins(String playerName) {
        if (!rejoins.containsKey(playerName)) return false;
        return rejoins.get(playerName) > 3;
    }
}
