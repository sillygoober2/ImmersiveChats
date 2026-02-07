package github.sillygoober2.immersiveChats;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class YellCommand implements CommandExecutor {
    private final ImmersiveChats plugin;

    public YellCommand(ImmersiveChats plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        double radius = plugin.getConfig().getDouble("yell-radius");
        String tag = plugin.getConfig().getString("yell-speech-tag");

        String plainText = String.join(" ",args);

        Component formattedMessage = plugin.formatMessage(sender.getName(), plainText, tag, 2);
        plugin.sendImmersiveChat((Player) sender, radius, formattedMessage);
        return true;
    }
}
