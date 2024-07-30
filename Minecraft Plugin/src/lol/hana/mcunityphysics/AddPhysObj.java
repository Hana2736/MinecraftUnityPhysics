package lol.hana.mcunityphysics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import javax.print.DocFlavor;

public class AddPhysObj implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
//get the player who sent the command
        if(Container.netClient == null || !Container.netClient.connected)
            return false;
        if (!(commandSender instanceof Player player)) {
            return false;
        }
        if(strings.length != 4)
            return false;
       //get the block type out of the command
        Material mat = Material.getMaterial(strings[0].toUpperCase());
        if(mat==null)
            return false;
        //find the player and create their BlockDisplay
        World world = player.getWorld();
        Location loc = new Location(world, Double.parseDouble(strings[1]), Double.parseDouble(strings[2]), Double.parseDouble(strings[3]));
        BlockDisplay physicsBlock = (BlockDisplay) world.spawnEntity(loc, EntityType.BLOCK_DISPLAY);
        physicsBlock.setBlock(mat.createBlockData());
        //add interpolation so it isn't stuttery in game
        physicsBlock.setInterpolationDelay(2);
        physicsBlock.setTeleportDuration(2);
        physicsBlock.setBrightness(new Display.Brightness(15,15));
        //track it and send it to Unity for simulation
        Container.trackedEntities.put(Container.currentEnt, physicsBlock);
        Container.netClient.sendMsg("AddPhys??"+Container.currentEnt+"??"+strings[3]+"??"+strings[2]+"??"+strings[1]);
        Util.sendMsg("Added new "+mat.name()+" physics block (ID "+Container.currentEnt+")");
        Container.currentEnt++;
        return true;
    }
}
