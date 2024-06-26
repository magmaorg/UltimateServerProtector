package ru.overwrite.protect;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import ru.overwrite.protect.api.ServerProtectorAPI;
import ru.overwrite.protect.api.ServerProtectorPasswordEnterEvent;
import ru.overwrite.protect.api.ServerProtectorPasswordFailEvent;
import ru.overwrite.protect.api.ServerProtectorPasswordSuccessEvent;
import ru.overwrite.protect.utils.Config;
import ru.overwrite.protect.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PasswordHandler {
    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final Config pluginConfig;
    public final Map<String, Integer> attempts = new HashMap<>();
    public final Map<String, BossBar> bossbars = new HashMap<>();

    public PasswordHandler(ServerProtectorManager plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
        api = plugin.getPluginAPI();
    }

    public void checkPassword(Player p, String input, boolean resync) {
        Runnable run =
                () -> {
                    ServerProtectorPasswordEnterEvent enterEvent =
                            new ServerProtectorPasswordEnterEvent(p, input);
                    if (pluginConfig.secure_settings_call_event_on_password_enter) {
                        enterEvent.callEvent();
                    }
                    if (enterEvent.isCancelled()) {
                        return;
                    }
                    if (pluginConfig.per_player_passwords.get(p.getName()) == null) {
                        failedPassword(p);
                        return;
                    }
                    String playerPass = pluginConfig.per_player_passwords.get(p.getName());
                    if (input.equals(playerPass)) {
                        correctPassword(p);
                        return;
                    }
                    failedPassword(p);
                    if (isAttemptsMax(p.getName())) {
                        plugin.checkFail(
                                p.getName(),
                                plugin.getConfig().getStringList("commands.failed-pass"));
                    }
                };
        if (resync) {
            plugin.getRunner().runPlayer(run, p);
        } else {
            run.run();
        }
    }

    private boolean isAttemptsMax(String playerName) {
        if (!attempts.containsKey(playerName)) return false;
        return attempts.get(playerName) >= 3;
    }

    public void failedPassword(Player p) {
        String playerName = p.getName();
        attempts.put(playerName, attempts.getOrDefault(playerName, 0) + 1);
        ServerProtectorPasswordFailEvent failEvent =
                new ServerProtectorPasswordFailEvent(p, attempts.get(playerName));
        failEvent.callEvent();
        if (failEvent.isCancelled()) {
            return;
        }
        p.sendMessage(pluginConfig.msg_incorrect);
        Utils.sendTitleMessage(pluginConfig.titles_incorrect, p);
        p.playSound(p.getLocation(), Sound.valueOf("ENTITY_VILLAGER_NO"), 1.0f, 1.0f);
        plugin.logAction("log-format.failed", p, new Date());
        plugin.sendAlert(p, pluginConfig.broadcasts_failed);
    }

    public void correctPassword(Player p) {
        ServerProtectorPasswordSuccessEvent successEvent =
                new ServerProtectorPasswordSuccessEvent(p);
        successEvent.callEvent();
        if (successEvent.isCancelled()) {
            return;
        }
        api.uncapturePlayer(p);
        p.sendMessage(pluginConfig.msg_correct);
        Utils.sendTitleMessage(pluginConfig.titles_correct, p);
        String playerName = p.getName();
        plugin.time.remove(playerName);
        p.playSound(p.getLocation(), Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 1.0f, 1.0f);
        for (PotionEffect s : p.getActivePotionEffects()) {
            p.removePotionEffect(s.getType());
        }
        this.showPlayer(p);
        api.authorisePlayer(p);
        plugin.getRunner()
                .runDelayedAsync(
                        () -> {
                            if (!api.isAuthorised(p)) {
                                api.deauthorisePlayer(p);
                            }
                        },
                        86400 * 20L);
        plugin.logAction("log-format.passed", p, new Date());
        if (bossbars.get(playerName) != null) {
            bossbars.get(playerName).removeAll();
        }
        plugin.sendAlert(p, pluginConfig.broadcasts_passed);
    }

    private void showPlayer(Player p) {
        if (pluginConfig.blocking_settings_hide_on_entering) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(p)) {
                    onlinePlayer.showPlayer(plugin, p);
                }
            }
        }
        if (pluginConfig.blocking_settings_hide_other_on_entering) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, onlinePlayer);
            }
        }
    }
}
