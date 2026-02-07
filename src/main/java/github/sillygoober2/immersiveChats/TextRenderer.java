package github.sillygoober2.immersiveChats;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class TextRenderer {
    private final ImmersiveChats plugin;

    public TextRenderer(ImmersiveChats plugin) {
        this.plugin = plugin;
    }

    public ArmorStand createText(Player player, Component text){
        Location spawnLoc = player.getLocation().add(0, 2.1, 0);

        ArmorStand armorStand = player.getWorld().spawn(spawnLoc, ArmorStand.class, entity -> {
            entity.setInvisible(true);
            entity.setMarker(true);

            entity.setCustomNameVisible(true);
            entity.customName(text);
            entity.setGravity(false);
            entity.setPersistent(false);
        });
        return armorStand;
    }
}
