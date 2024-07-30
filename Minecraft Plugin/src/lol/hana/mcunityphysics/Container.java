package lol.hana.mcunityphysics;

import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class Container {
    public static Map<Integer, Entity> trackedEntities;
    public static Map<UUID, Integer> playerToTracked;
    public static int currentEnt = 0;
    public static NetClient netClient;
    public static Map<UUID, double[]> playerPosVels;
    public static Map<UUID, Long> lastPlayerMoveEvent;


    public static void sendPlayerUpdate(UUID playID) {
        Player play = Util.server().getPlayer(playID);
        var pLoc = play.getLocation();
        double playerX = pLoc.getX();
        double playerY = pLoc.getY();
        double playerZ = pLoc.getZ();
        var velVec = playerPosVels.get(playID);
        double playerVX = velVec[0];
        double playerVY = velVec[1];
        double playerVZ = velVec[2];
        int trackedID = Container.playerToTracked.get(playID);
        //send Unity our current player state (angle doesnt matter because Minecraft hitboxes dont rotate)
        Container.netClient.sendMsg("PlayerUpdate??" + trackedID + "??" +
                playerZ + "??" + playerY + "??" + playerX + "??" +
                playerVZ + "??" + playerVY + "??" + playerVX);
    }


    //handle incoming data from server
    public static void handleIncoming() {
        if (!netClient.connected)
            return;
        while (!netClient.incomingMsgs.isEmpty()) {
            String toDo = netClient.incomingMsgs.poll();
            String[] parts = toDo.split(Pattern.quote("??"));
            //this switch is kinda redundant, but it makes it easy to add more messages
            switch (parts[0]) {
                case "PhysUpdate": {
                    int id = Integer.parseInt(parts[1]);
                    double posX = Double.parseDouble(parts[2]);
                    double posY = Double.parseDouble(parts[3]);
                    double posZ = Double.parseDouble(parts[4]);
                    float[] rotate = new float[16];
                    for (int i = 5; i < parts.length; i++) {
                        rotate[i - 5] = Float.parseFloat(parts[i]);
                    }
                    BlockDisplay found = (BlockDisplay) trackedEntities.get(id);
                    found.setInterpolationDuration(2);
                    found.teleport(new Location(found.getWorld(), posX, posY, posZ));
                    //this is really ugly, but using a FloatStream consistently crashed the entire JVM, so oh well
                    found.setTransformationMatrix(new Matrix4f(rotate[0],rotate[1],rotate[2],rotate[3],rotate[4],rotate[5],rotate[6],rotate[7],rotate[8],rotate[9],rotate[10],rotate[11],rotate[12],rotate[13],rotate[14],rotate[15]));
                    break;
                }
            }
        }


        Util.runLater(Container::handleIncoming, 1);
    }
}
