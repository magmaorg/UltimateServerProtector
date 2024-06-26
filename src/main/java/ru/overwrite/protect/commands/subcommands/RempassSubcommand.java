package ru.overwrite.protect.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import ru.overwrite.protect.ServerProtectorManager;

public class RempassSubcommand extends AbstractSubCommand {
    public RempassSubcommand(ServerProtectorManager plugin) {
        super(plugin, "rempass", "serverprotector.rempass", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            if (!plugin.isAdmin(args[1]) && !plugin.isAdmin(args[1])) {
                sender.sendMessage(pluginConfig.uspmsg_notinconfig);
                return true;
            }
            if (args.length < 3) {
                removeAdmin(args[1]);
                sender.sendMessage(pluginConfig.uspmsg_playerremoved);
                return true;
            }
        }
        sendCmdUsage(sender, pluginConfig.uspmsg_rempassusage, label);
        return true;
    }

    private void removeAdmin(String nick) {
        FileConfiguration dataFile;
        dataFile = pluginConfig.getFile(plugin.path, plugin.dataFileName);
        dataFile.set("data." + nick + ".pass", null);
        dataFile.set("data." + nick, null);
        dataFile.set("data." + nick, null);
        pluginConfig.save(plugin.path, dataFile, plugin.dataFileName);
        plugin.dataFile = dataFile;
    }
}
