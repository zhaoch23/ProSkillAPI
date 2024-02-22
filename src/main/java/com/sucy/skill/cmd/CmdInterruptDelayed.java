package com.sucy.skill.cmd;

import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.rit.sucy.version.VersionManager;
import com.sucy.skill.api.util.DelayedTaskManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CmdInterruptDelayed implements IFunction {
    private static final String NOT_PLAYER   = "not-player";
    private static final String SUCCESS      = "success";
    @Override
    public void execute(ConfigurableCommand command, Plugin plugin, CommandSender sender, String[] args) {
        if (args.length != 1) {
            command.displayHelp(sender);
        }

        OfflinePlayer player = VersionManager.getOfflinePlayer(args[0], false);
        if (player == null)
        {
            command.sendMessage(sender, NOT_PLAYER, "&4That is not a valid player name");
            return;
        }

        DelayedTaskManager.clearTasks(player.getUniqueId().toString());
        command.sendMessage(sender, SUCCESS, "Cleared all delayed tasks for " + args[0]);
    }
}
