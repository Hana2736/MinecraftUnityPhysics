package lol.hana.mcunityphysics;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class EventListeners implements Listener {
    //this function is called every time a player moves on the server
    @EventHandler
    public void movePlayer(PlayerMoveEvent event) {
        if(Container.netClient == null || !Container.netClient.connected)
            return;

        UUID id = event.getPlayer().getUniqueId();
        long newTimeStamp = System.currentTimeMillis();

        //If this is their first movement, assume they moved 1 tick ago for this math.
        long oldTimeStamp = newTimeStamp - 50;
        if (Container.lastPlayerMoveEvent.containsKey(id))
            oldTimeStamp = Container.lastPlayerMoveEvent.get(id);

        //Check how long it's been since the last time the player moved
        long deltaTime = newTimeStamp - oldTimeStamp;
        Container.lastPlayerMoveEvent.put(id, newTimeStamp);
        Location old = event.getFrom();
        Location now = event.getTo();
        //Calculate movement velocity based on their times
        float[] vel = new float[3];
        double dX = now.getX() - old.getX();
        double dY = now.getY() - old.getY();
        double dZ = now.getZ() - old.getZ();
        //Util.sendMsg("deltaTime="+deltaTime+" dX="+dX+" dY="+dY+" dZ="+dZ);
        vel[0] = (float) (dX / deltaTime * 1000);
        vel[1] = (float) (dY / deltaTime * 1000);
        vel[2] = (float) (dZ / deltaTime * 1000);
        Container.playerPosVels.put(id,vel);

        //send the player pos now
        Container.sendPlayerUpdate(id);
    }
}
