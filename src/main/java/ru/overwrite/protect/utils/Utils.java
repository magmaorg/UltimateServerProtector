package ru.overwrite.protect.utils;

import org.bukkit.entity.Player;

public final class Utils {
    public static String getIp(Player player) {
        return player.getAddress().getAddress().getHostAddress();
    }

    public static void sendTitleMessage(String[] titleMessages, Player p) {
        p.sendTitle(titleMessages[0], titleMessages[1], 10, 60, 15);
    }
}
