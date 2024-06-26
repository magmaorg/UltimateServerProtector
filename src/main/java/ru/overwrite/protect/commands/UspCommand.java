package ru.overwrite.protect.commands;

import org.bukkit.command.*;

import ru.overwrite.protect.ServerProtectorManager;
import ru.overwrite.protect.commands.subcommands.*;
import ru.overwrite.protect.utils.Config;

import java.util.*;

public class UspCommand implements CommandExecutor, TabCompleter {
    private final ServerProtectorManager plugin;
    private final Config pluginConfig;

    private final Map<String, AbstractSubCommand> subCommands = new HashMap<>();

    public UspCommand(ServerProtectorManager plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
        registerSubCommand(new LogoutSubcommand(plugin));
        registerSubCommand(new ReloadSubcommand(plugin));
        registerSubCommand(new RebootSubcommand(plugin));
        registerSubCommand(new SetpassSubcommand(plugin));
        registerSubCommand(new RempassSubcommand(plugin));
    }

    private void registerSubCommand(AbstractSubCommand subCmd) {
        subCommands.put(subCmd.getName(), subCmd);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }
        AbstractSubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand != null) {
            if (subCommand.isAdminCommand()) {
                if (pluginConfig.secure_settings_only_console_usp
                        && !(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage(pluginConfig.uspmsg_consoleonly);
                    return false;
                }
            }
            if (!sender.hasPermission(subCommand.getPermission())) {
                sendHelp(sender, label);
                return false;
            }
            return subCommand.execute(sender, label, args);
        }
        if (sender.hasPermission("serverprotector.protect")) {
            sendHelp(sender, label);
            return true;
        }
        sender.sendMessage(
                "§6❖ §7Running §c§lUltimateServerProtector "
                        + plugin.getDescription().getVersion()
                        + "§7 by §5OverwriteMC");
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sendCmdMessage(sender, pluginConfig.uspmsg_usage, label, "serverprotector.protect");
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_logout, label, "serverprotector.protect");
        if (!sender.hasPermission("serverprotector.admin")) {
            return;
        }
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_reload, label, "serverprotector.reload");
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_reboot, label, "serverprotector.reboot");
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_setpass, label, "serverprotector.setpass");
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_rempass, label, "serverprotector.rempass");
    }

    private void sendCmdMessage(CommandSender sender, String msg, String label, String permission) {
        if (sender.hasPermission(permission)) {
            sender.sendMessage(msg.replace("%cmd%", label));
        }
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender, Command command, String alias, String[] args) {
        if (pluginConfig.secure_settings_only_console_usp
                && !(sender instanceof ConsoleCommandSender)) {
            return Collections.emptyList();
        }
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("logout");
            completions.add("reload");
            completions.add("reboot");
            completions.add("setpass");
            completions.add("rempass");
        }
        List<String> result = new ArrayList<>();
        for (String c : completions) {
            if (c.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) result.add(c);
        }
        return result;
    }
}
