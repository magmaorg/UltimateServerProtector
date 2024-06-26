package ru.overwrite.protect.commands.subcommands;

import org.bukkit.command.CommandSender;

import ru.overwrite.protect.ServerProtectorManager;

public class ReloadSubcommand extends AbstractSubCommand {
    public ReloadSubcommand(ServerProtectorManager plugin) {
        super(plugin, "reload", "serverprotector.reload", false);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        plugin.getRunner().cancelTasks();
        plugin.reloadConfigs();
        plugin.time.clear();
        api.login.clear();
        api.ips.clear();
        api.saved.clear();
        for (String playerName : passwordHandler.bossbars.keySet())
            passwordHandler.bossbars.get(playerName).removeAll();
        passwordHandler.attempts.clear();
        plugin.startTasks(plugin.getConfig());
        sender.sendMessage(pluginConfig.uspmsg_reloaded);
        return true;
    }
}
