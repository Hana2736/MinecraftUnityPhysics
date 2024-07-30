package lol.hana.mcunityphysics;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class SetupCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        //get the player who sent the command
        if (!(commandSender instanceof Player player)) {
            return false;
        }
        if (strings.length != 2)
            return false;
        if (!strings[0].contains(":"))
            return false;
        //get how many blocks to load in Unity
        int radius = Integer.parseInt(strings[1]);
        String[] parts = strings[0].split(":");
        int port = Integer.parseInt(parts[1]);

        //connect to Unity server
        Container.netClient = new NetClient(parts[0], port);
        if (!Container.netClient.connected)
            return false;
        Util.sendMsg("Connected...");

        Container.playerPosVels = new HashMap<>();
        Container.lastPlayerMoveEvent = new HashMap<>();
        Container.trackedEntities = new HashMap<>();
        Container.playerToTracked = new HashMap<>();
        Container.currentEnt = 0;
        //store all our players as intermediary entities and send them to the Unity server
        var players = player.getWorld().getPlayers();
        for (var curPlayer : players) {
            Container.trackedEntities.put(Container.currentEnt, curPlayer);
            Container.playerToTracked.put(curPlayer.getUniqueId(), Container.currentEnt);
            Container.netClient.sendMsg("AddPlayer??" + Container.currentEnt + "??" + player.getName());
            Container.currentEnt++;
        }
        Util.sendMsg("Added players to simulation.");
        Util.sendMsg("Sending blocks... This is slow! You will maybe lag out of the game");
        //Get player position and get all the blocks in the radius
        World playWorld = player.getWorld();
        int startX = player.getLocation().getBlockX();
        int startY = player.getLocation().getBlockY();
        int startZ = player.getLocation().getBlockZ();
        startX -= radius;
        startY -= radius;
        startZ -= radius;
        radius *= 2;
        int blocksSent = 0;
        for (int x = startX; x <= startX + radius; x++) {
            for (int y = startY; y <= startY + radius; y++) {
                for (int z = startZ; z <= startZ + radius; z++) {
                    try {
                        if (playWorld.getBlockAt(x, y, z).getType().isAir())
                            continue;
                        //check if the block is fully covered. if it is, save Unity the CPU work and skip it.
                        //Yandere Dev was here
                        int facesCovered = 0;
                        if (!playWorld.getBlockAt(x - 1, y, z).getType().isAir())
                            facesCovered++;
                        if (!playWorld.getBlockAt(x + 1, y, z).getType().isAir())
                            facesCovered++;
                        if (!playWorld.getBlockAt(x, y - 1, z).getType().isAir())
                            facesCovered++;
                        if (!playWorld.getBlockAt(x, y + 1, z).getType().isAir())
                            facesCovered++;
                        if (!playWorld.getBlockAt(x, y, z - 1).getType().isAir())
                            facesCovered++;
                        if (!playWorld.getBlockAt(x, y, z + 1).getType().isAir())
                            facesCovered++;
                        if (facesCovered == 6)
                            continue;
                        blocksSent++;
                        //If we send too many messages too fast, we will go over the Unity buffer, so spread it out a bit
                        if(blocksSent % 10 ==0)
                            Thread.sleep(1);
                        //stupid Minecraft has XZ inverted so we'll swap it for Unity
                        Container.netClient.sendMsg("WorldBlock??" + z + "??" + y + "??" + x);
                    } catch (Exception e) {
                        //we probably cant load or reach this block, oh well
                    }
                }
            }
        }
        Util.sendMsg("Sent " + blocksSent + " blocks to Unity!");


        //start updating Unity with player locations every tick
        Util.runLater(this::updatePlayers, 1);
        Util.runLater(Container::handleIncoming, 1);
        return true;
    }

    public void updatePlayers() {
        //since we normally only send the velocity while the player is moving, we need to stop them at some point.
        if (!Container.netClient.connected)
            return;
        for (var pair : Container.playerPosVels.entrySet()) {
            //If they havent moved in ~100ms, we will stop the player
            if (System.currentTimeMillis() - Container.lastPlayerMoveEvent.get(pair.getKey()) > 2 * 50) {
                Container.playerPosVels.put(pair.getKey(), new double[]{0, 0, 0});
                Container.sendPlayerUpdate(pair.getKey());
            }
        }
        Util.runLater(this::updatePlayers, 1);
    }
}
