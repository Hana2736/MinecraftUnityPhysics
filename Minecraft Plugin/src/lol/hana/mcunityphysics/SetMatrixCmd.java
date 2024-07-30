package lol.hana.mcunityphysics;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.joml.Matrix4f;

public class SetMatrixCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length != 17)
            return false;
        BlockDisplay disp = (BlockDisplay) Container.trackedEntities.get(Integer.parseInt(strings[0]));
       //Yandere Dev was here- this is a debug function :))))
        var mat= new Matrix4f(Float.parseFloat(strings[1]),Float.parseFloat(strings[2]),Float.parseFloat(strings[3]),Float.parseFloat(strings[4]),
                     Float.parseFloat(strings[5]), Float.parseFloat(strings[6]),Float.parseFloat(strings[7]),Float.parseFloat(strings[8]),
                     Float.parseFloat(strings[9]),Float.parseFloat(strings[10]),Float.parseFloat(strings[11]),Float.parseFloat(strings[12]),
                Float.parseFloat(strings[13]),Float.parseFloat(strings[14]),Float.parseFloat(strings[15]),Float.parseFloat(strings[16]));
        disp.setTransformationMatrix(mat);
    return true;
    }
}
