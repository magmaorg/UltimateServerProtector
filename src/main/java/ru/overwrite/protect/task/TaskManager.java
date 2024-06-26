package ru.overwrite.protect.task;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import ru.overwrite.protect.PasswordHandler;
import ru.overwrite.protect.ServerProtectorManager;
import ru.overwrite.protect.api.CaptureReason;
import ru.overwrite.protect.api.ServerProtectorAPI;
import ru.overwrite.protect.api.ServerProtectorCaptureEvent;
import ru.overwrite.protect.utils.Config;
import ru.overwrite.protect.utils.Utils;

import java.util.Date;

public final class TaskManager {
    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final PasswordHandler passwordHandler;
    private final Config pluginConfig;
    private final Runner runner;

    public TaskManager(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.api = plugin.getPluginAPI();
        this.passwordHandler = plugin.getPasswordHandler();
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    public void startMainCheck() {
        runner.runPeriodicalAsync(
                () -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (plugin.isExcluded(p, pluginConfig.excluded_players)) continue;
                        if (api.isCaptured(p)) continue;
                        CaptureReason captureReason = plugin.checkPermissions(p);
                        if (captureReason == null) continue;
                        if (!api.isAuthorised(p)) {
                            ServerProtectorCaptureEvent captureEvent =
                                    new ServerProtectorCaptureEvent(
                                            p, Utils.getIp(p), captureReason);
                            captureEvent.callEvent();
                            if (captureEvent.isCancelled()) continue;
                            api.capturePlayer(p);
                            p.playSound(
                                    p.getLocation(),
                                    Sound.valueOf("ENTITY_ITEM_BREAK"),
                                    1.0f,
                                    1.0f);
                            plugin.giveEffect(p);
                            plugin.applyHide(p);
                            plugin.logAction("log-format.captured", p, new Date());
                            plugin.sendAlert(p, pluginConfig.broadcasts_captured);
                        }
                    }
                },
                20L,
                40L);
    }

    public void startAdminCheck(FileConfiguration config) {
        runner.runPeriodicalAsync(
                () -> {
                    if (api.login.isEmpty()) return;
                    for (Player p : Bukkit.getOnlinePlayers())
                        if (api.isCaptured(p) && !plugin.isAdmin(p.getName()))
                            plugin.checkFail(
                                    p.getName(), config.getStringList("commands.not-in-config"));
                },
                0L,
                20L);
    }

    public void startCapturesMessages(FileConfiguration config) {
        runner.runPeriodicalAsync(
                () -> {
                    if (api.login.isEmpty()) return;
                    for (Player p : Bukkit.getOnlinePlayers())
                        if (api.isCaptured(p)) {
                            p.sendMessage(this.pluginConfig.msg_message);
                            Utils.sendTitleMessage(this.pluginConfig.titles_message, p);
                        }
                },
                0L,
                40L);
    }

    public void startCapturesTimer(FileConfiguration config) {
        runner.runPeriodicalAsync(
                () -> {
                    if (api.login.isEmpty()) return;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!api.isCaptured(p)) return;
                        String playerName = p.getName();
                        if (!plugin.time.containsKey(playerName)) {
                            plugin.time.put(playerName, 0);
                            BossBar bossbar =
                                    Bukkit.createBossBar(
                                            "§fСекунд осталось: §c60",
                                            BarColor.valueOf("RED"),
                                            BarStyle.valueOf("SEGMENTED_12"));
                            bossbar.addPlayer(p);
                            passwordHandler.bossbars.put(playerName, bossbar);
                        } else {
                            plugin.time.compute(playerName, (k, currentTime) -> currentTime + 1);
                            int newTime = plugin.time.get(playerName);
                            if (passwordHandler.bossbars.get(playerName) != null) {
                                passwordHandler
                                        .bossbars
                                        .get(playerName)
                                        .setTitle(
                                                "§fСекунд осталось: §c"
                                                        + Integer.toString(60 - newTime));
                                double percents = (60 - newTime) / (double) 60;
                                if (percents > 0) {
                                    passwordHandler.bossbars.get(playerName).setProgress(percents);
                                    passwordHandler.bossbars.get(playerName).addPlayer(p);
                                }
                            }
                        }
                        if (!noTimeLeft(playerName)) {
                            plugin.checkFail(
                                    playerName, config.getStringList("commands.failed-time"));
                            passwordHandler.bossbars.get(playerName).removePlayer(p);
                        }
                    }
                },
                0L,
                20L);
    }

    private boolean noTimeLeft(String playerName) {
        return !plugin.time.containsKey(playerName) || plugin.time.get(playerName) < 60;
    }
}
