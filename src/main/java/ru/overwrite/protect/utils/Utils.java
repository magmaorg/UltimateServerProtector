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
        if (titleMessages.length > 5) {
            Bukkit.getConsoleSender()
                    .sendMessage("Unable to send title. " + titleMessages.toString());
            return;
        }
        String title = titleMessages[0];
        String subtitle =
                (titleMessages.length >= 2 && titleMessages[1] != null) ? titleMessages[1] : "";
        int fadeIn =
                (titleMessages.length >= 3 && titleMessages[2] != null)
                        ? Integer.parseInt(titleMessages[2])
                        : 10;
        int stay =
                (titleMessages.length >= 4 && titleMessages[3] != null)
                        ? Integer.parseInt(titleMessages[3])
                        : 70;
        int fadeOut =
                (titleMessages.length == 5 && titleMessages[4] != null)
                        ? Integer.parseInt(titleMessages[4])
                        : 20;
        p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
}
