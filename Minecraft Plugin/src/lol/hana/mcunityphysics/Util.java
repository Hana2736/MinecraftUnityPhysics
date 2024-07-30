package lol.hana.mcunityphysics;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;

public class Util {
    //This is a basic Util class i share with other Minecraft projects

    //send a chat message to a player
    public static void sendMsg(String msg, Player p) {
        TextComponent message = new TextComponent(ChatColor.RED + "MinecraftUnityPhysics >" + ChatColor.RESET + ' '+ msg);
        p.spigot().sendMessage(message);
    }

    //send a chat message to all players
    public static void sendMsg(String msg) {
        TextComponent message = new TextComponent(msg);
        for (var player : server().getOnlinePlayers()) {
            sendMsg(msg, player);
        }
    }

    //runs a piece of code in X ticks, so 1 tick will run next cycle, and 0 runs now.
    public static void runLater(Runnable code, int delayTicks) {

        new BukkitRunnable() {
            @Override
            public void run() {
                code.run();
            }
        }.runTaskLater(MinecraftUnityPhysics.getProvidingPlugin(MinecraftUnityPhysics.class),delayTicks);
    }

    //run a console command like a command block
    public static void sendConsoleCommand(String s) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
    }

    //rest of this is not really used in this project
    public static Player findPlayerByName(String s){
       return server().getPlayer(s);
    }

    public static Player findPlayerByUUID(String s){
       return server().getPlayer(UUID.fromString(s));
    }

    public static Server server(){
        return JavaPlugin.getProvidingPlugin(MinecraftUnityPhysics.class).getServer();
    }

    public static int quickRandom(int minIn, int maxEx) {
        var r = new Random();
        return r.nextInt(minIn, maxEx);
    }

    public static Path configDir;

    public static class Tuple<X, Y> {
        public final X x;
        public final Y y;
        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }
}
