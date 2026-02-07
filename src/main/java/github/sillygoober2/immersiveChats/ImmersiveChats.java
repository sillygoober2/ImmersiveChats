package github.sillygoober2.immersiveChats;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class ImmersiveChats extends JavaPlugin implements Listener {

    List<String> puncList = Arrays.asList(".","?",",","!",";");

    public Map<UUID, List<ArmorStand>> textBubbles = new HashMap<>();

    private TextRenderer textRenderer;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.textRenderer = new TextRenderer(this);

        getCommand("icreload").setExecutor(new ReloadCommand(this));
        getCommand("yell").setExecutor(new YellCommand(this));
        getCommand("whisper").setExecutor(new WhisperCommand(this));

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onChatMessage(AsyncChatEvent event){
        Player player = event.getPlayer();

        event.viewers().removeIf(audience -> {
            if(audience instanceof Player){
                return true;
            }
            return false;
        });

        String plainText = PlainTextComponentSerializer.plainText().serialize(event.message());

        double radius;
        String tag;
        int type;
        if(getConfig().getBoolean("whisper-when-sneaking") && player.isSneaking()){
            radius = getConfig().getDouble("whisper-radius");
            tag = getConfig().getString("whisper-speech-tag");
            type = 3;
        } else if (getConfig().getBoolean("yell-when-capitalized") && isAllCaps(plainText)) {
            radius = getConfig().getDouble("yell-radius");
            tag = getConfig().getString("yell-speech-tag");
            type = 2;
        } else {
            radius = getConfig().getDouble("chat-radius");
            tag = getConfig().getString("chat-speech-tag");
            type = 1;
        }

        Component formattedMessage = formatMessage(player.getName(), plainText, tag, type);
        sendImmersiveChat(player, radius, formattedMessage);
    }

    public void sendImmersiveChat(Player sender, double radius, Component message){
        getServer().getScheduler().runTask(this, () -> {
            ArmorStand armorStand;
            if(getConfig().getBoolean("create-text")){
                UUID uuid = sender.getUniqueId();
                List<ArmorStand> playerBubbles = textBubbles.computeIfAbsent(uuid, k -> new ArrayList<>());

                for(ArmorStand oldStand: playerBubbles){
                    oldStand.teleport(oldStand.getLocation().add(0, 0.3, 0));
                }

                armorStand = textRenderer.createText(sender, message);
                playerBubbles.add(armorStand);

                for(Player online : getServer().getOnlinePlayers()){
                    online.hideEntity(this, armorStand);
                }
            } else {
                armorStand = null;
            }

            for(Player recipient: sender.getWorld().getPlayers()){
                double distance = sender.getLocation().distance(recipient.getLocation());
                if(distance <= radius){
                    recipient.sendMessage(message);
                    if(armorStand != null && recipient != sender) {
                        recipient.showEntity(this, armorStand);
                    }
                }
            }

            if(armorStand == null) return;

            long ticks = getConfig().getInt("text-duration-seconds") * 20L;
            getServer().getScheduler().runTaskLater(this, armorStand::remove, ticks);

            if(!getConfig().getBoolean("create-text")) return;
            new BukkitRunnable() {
                int life = 0;

                public void run(){
                    List<ArmorStand> playerBubbles = textBubbles.computeIfAbsent(sender.getUniqueId(), k -> new ArrayList<>());
                    if (life > 100 || !sender.isOnline()) {
                        this.cancel();
                        playerBubbles.remove(armorStand);
                        return;
                    }

                    double yOffset = 2.1 + (playerBubbles.size() - playerBubbles.indexOf(armorStand)) * getConfig().getDouble("text-spacing");
                    armorStand.teleport(sender.getLocation().add(0, yOffset, 0));

                    life++;
                }
            }.runTaskTimer(this, 0L, 1L);
        });

    }

    public Component formatMessage(String sender, String text, String tag, int type){
        if(getConfig().getBoolean("uppercase-first")) {
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
        }

        if(getConfig().getBoolean("add-full-stop") && puncList.stream().noneMatch(text::endsWith)){
            text = text+".";
        }

        Component quotes = Component.text('"');
        Component message = Component.text(sender);

        if(type == 1){
            message = message.append(Component.text(" "+tag+", ", NamedTextColor.YELLOW))
                    .append(quotes)
                    .append(Component.text(text))
                    .append(quotes);
        } else if(type == 2){
            quotes = quotes.color(NamedTextColor.RED);

            message = message.append(Component.text(" "+tag+", "))
                    .append(quotes)
                    .append(Component.text(text, NamedTextColor.RED))
                    .append(quotes);
        } else if(type == 3){
            message = message.append(Component.text(" "+tag+", "))
                    .append(quotes)
                    .append(Component.text(text))
                    .append(quotes);
            message = message.color(NamedTextColor.DARK_GRAY);
        }

        return message;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event){
        String message = event.getMessage().toLowerCase();
        if(getConfig().getBoolean("disable-private-messaging") &&
           message.startsWith("/msg ") ||
           message.startsWith("/tell ")){

            event.setCancelled(true);
            event.getPlayer().sendMessage("Private messaging is disabled");
        }
    }

    public boolean isAllCaps(String text){
        return text.equals(text.toUpperCase()) && !text.equals(text.toLowerCase());
    }
}
