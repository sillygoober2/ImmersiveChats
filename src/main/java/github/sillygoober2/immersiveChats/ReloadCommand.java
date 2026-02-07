package github.sillygoober2.immersiveChats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final ImmersiveChats plugin;

    public ReloadCommand(ImmersiveChats plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(sender.hasPermission("immersivechats.reloadConfig")) {
            plugin.reloadConfig();
            sender.sendMessage("Reloaded config");
            plugin.getLogger().info(sender.getName()+" reloaded the Immersive Chats config");
            return true;
        }
        sender.sendMessage("You don't have the permissions to run this command");
        return true;
    }
}
