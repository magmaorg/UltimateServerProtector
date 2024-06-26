package ru.overwrite.protect.utils;

import static net.md_5.bungee.api.ChatColor.COLOR_CHAR;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import ru.overwrite.protect.ServerProtectorManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
    public static final int SUB_VERSION =
            Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    public static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
    }

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

    public static void sendSound(String[] soundArgs, Player p) {
        if (soundArgs.length > 3) {
            Bukkit.getConsoleSender().sendMessage("Unable to send sound. " + soundArgs.toString());
            return;
        }
        Sound sound = Sound.valueOf(soundArgs[0]);
        float volume =
                (soundArgs.length >= 2 && soundArgs[1] != null)
                        ? Float.parseFloat(soundArgs[1])
                        : 1.0f;
        float pitch =
                (soundArgs.length == 3 && soundArgs[2] != null)
                        ? Float.parseFloat(soundArgs[2])
                        : 1.0f;
        p.playSound(p.getLocation(), sound, volume, pitch);
    }

    public static String colorize(String message, String serializer) {
        switch (serializer) {
            case "LEGACY":
                {
                    if (SUB_VERSION >= 16) {
                        Matcher matcher = HEX_PATTERN.matcher(message);
                        StringBuilder builder = new StringBuilder(message.length() + 32);
                        while (matcher.find()) {
                            String group = matcher.group(1);
                            matcher.appendReplacement(
                                    builder,
                                    COLOR_CHAR
                                            + "x"
                                            + COLOR_CHAR
                                            + group.charAt(0)
                                            + COLOR_CHAR
                                            + group.charAt(1)
                                            + COLOR_CHAR
                                            + group.charAt(2)
                                            + COLOR_CHAR
                                            + group.charAt(3)
                                            + COLOR_CHAR
                                            + group.charAt(4)
                                            + COLOR_CHAR
                                            + group.charAt(5));
                        }
                        message = matcher.appendTail(builder).toString();
                    }
                    return ChatColor.translateAlternateColorCodes('&', message);
                }
            case "MINIMESSAGE":
                {
                    Component component = MiniMessage.miniMessage().deserialize(message);
                    return LegacyComponentSerializer.legacySection().serialize(component);
                }
            default:
                {
                    return message;
                }
        }
    }

    public static void checkUpdates(ServerProtectorManager plugin, Consumer<String> consumer) {
        plugin.getRunner()
                .runAsync(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    new URL(
                                                                    "https://raw.githubusercontent.com/Overwrite987/UltimateServerProtector/master/VERSION")
                                                            .openStream()))) {
                                consumer.accept(reader.readLine().trim());
                            } catch (IOException exception) {
                                plugin.getLogger()
                                        .warning(
                                                "Can't check for updates: "
                                                        + exception.getMessage());
                            }
                        });
    }
}
