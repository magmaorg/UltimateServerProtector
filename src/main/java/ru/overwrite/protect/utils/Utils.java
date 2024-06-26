package ru.overwrite.protect.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Utils {
    public static final int SUB_VERSION =
            Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);

    public static String getIp(Player player) {
        return player.getAddress().getAddress().getHostAddress();
    }

    public static void sendTitleMessage(String[] titleMessages, Player p) {
        if (titleMessages.length > 2) {
            Bukkit.getConsoleSender()
                    .sendMessage("Unable to send title. " + titleMessages.toString());
            return;
        }
        String title = titleMessages[0];
        String subtitle =
                (titleMessages.length >= 2 && titleMessages[1] != null) ? titleMessages[1] : "";
        p.sendTitle(title, subtitle, 10, 60, 15);
    }
}
