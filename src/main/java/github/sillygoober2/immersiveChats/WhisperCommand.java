package github.sillygoober2.immersiveChats;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WhisperCommand implements CommandExecutor {
    private final ImmersiveChats plugin;

    public WhisperCommand(ImmersiveChats plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        double radius = plugin.getConfig().getDouble("whisper-radius");
        plugin.getLogger().info(radius+"");
        String tag = plugin.getConfig().getString("whisper-speech-tag");

        String plainText = String.join(" ",args);

        Component formattedMessage = plugin.formatMessage(sender.getName(), plainText, tag, 3);
        plugin.sendImmersiveChat((Player) sender, radius, formattedMessage);
        return true;
    }
}
