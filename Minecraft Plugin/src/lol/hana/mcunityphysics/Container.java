package lol.hana.mcunityphysics;

import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.UUID;

public class Container {
    public static Map<Integer, Entity> trackedEntities;
    public static Map<UUID, Integer> playerToTracked;
    public static int currentEnt = 0;
    public static NetClient netClient;
    public static Map<UUID, float[]> playerPosVels;
    public static Map<UUID, Long> lastPlayerMoveEvent;


    public static void sendPlayerUpdate(UUID playID) {
        Player play = Util.server().getPlayer(playID);
        var pLoc = play.getLocation();
        float[] velVec = playerPosVels.get(playID);
        int trackedID = Container.playerToTracked.get(playID);
        //send Unity our current player state (angle doesnt matter because Minecraft hitboxes dont rotate)
        var pUpdate = NetMessages.PlayerUpdate.newBuilder();
        pUpdate.setPlayerID(trackedID);
        pUpdate.addPlayerCoords((float) pLoc.getZ());
        pUpdate.addPlayerCoords((float) pLoc.getY());
        pUpdate.addPlayerCoords((float) pLoc.getX());
        pUpdate.addPlayerVel(velVec[2]);
        pUpdate.addPlayerVel(velVec[1]);
        pUpdate.addPlayerVel(velVec[0]);
        netClient.sendMsg(pUpdate);
    }


    //handle incoming data from server
    public static void handleIncoming() {
        if (!netClient.connected)
            return;
        while (!netClient.incomingMsgs.isEmpty()) {
            var toDo = netClient.incomingMsgs.poll();
            //Util.sendMsg(toDo.getClass().toString());
            if (toDo instanceof NetMessages.PhysUpdate cast) {
                //Util.sendMsg("phys");
                int id = cast.getObjectID();
                var pos = cast.getObjectCoordsList();
                var transMtx = cast.getObjectTransMatrixList();
                float[] rotate = new float[16];
                for (int i = 0; i < transMtx.size(); i++) {
                    rotate[i] = transMtx.get(i);
                }
                BlockDisplay found = (BlockDisplay) trackedEntities.get(id);
                found.setInterpolationDuration(2);
                //Util.sendMsg("Pos: "+pos.get(0)+" "+pos.get(1)+" "+pos.get(2));
                found.teleport(new Location(found.getWorld(), pos.get(0), pos.get(1), pos.get(2)));
                //this is really ugly, but using a FloatStream consistently crashed the entire JVM, so oh well
                found.setTransformationMatrix(new Matrix4f(rotate[0], rotate[1], rotate[2], rotate[3], rotate[4], rotate[5], rotate[6], rotate[7], rotate[8], rotate[9], rotate[10], rotate[11], rotate[12], rotate[13], rotate[14], rotate[15]));
            }
        }
        Util.runLater(Container::handleIncoming, 1);
    }
}
