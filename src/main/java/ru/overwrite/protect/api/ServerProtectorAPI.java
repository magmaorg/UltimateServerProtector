package ru.overwrite.protect.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import ru.overwrite.protect.Logger;
import ru.overwrite.protect.ServerProtectorManager;
import ru.overwrite.protect.utils.Utils;

import java.util.HashSet;
import java.util.Set;

public class ServerProtectorAPI {
    private final Logger logger;
    public final Set<String> login = new HashSet<>();
    public final Set<String> ips = new HashSet<>();
    public final Set<String> saved = new HashSet<>();

    public ServerProtectorAPI(ServerProtectorManager plugin) {
        logger = plugin.getPluginLogger();
    }

    public boolean isCaptured(Player p) {
        if (this.login.isEmpty()) return false;

        return this.login.contains(p.getName());
    }

    public void capturePlayer(Player p) {
        if (isCaptured(p)) {
            logger.warn("Unable to capture " + p.getName() + " Reason: Already captured");
            return;
        }
        this.login.add(p.getName());
    }

    public void uncapturePlayer(Player p) {
        if (!isCaptured(p)) {
            logger.warn("Unable to uncapture " + p.getName() + " Reason: Not captured");
            return;
        }
        this.login.remove(p.getName());
    }

    public boolean isAuthorised(Player p) {
        return ips.contains(p.getName() + Utils.getIp(p));
    }

    public void authorisePlayer(Player p) {
        if (isAuthorised(p)) {
            logger.warn("Unable to authorise " + p.getName() + " Reason: Already authorised");
            return;
        }
        ips.add(p.getName() + Utils.getIp(p));
    }

    public void deauthorisePlayer(Player p) {
        if (!isAuthorised(p)) {
            logger.warn("Unable to deauthorise " + p.getName() + " Reason: Is not authorised");
            return;
        }
        ips.remove(p.getName() + Utils.getIp(p));
    }

    public void handleInteraction(Player p, Cancellable e) {
        if (isCaptured(p)) e.setCancelled(true);
    }
}
