package lol.hana.mcunityphysics;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FillPhys implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length != 7)
            return false;
        double x = Double.parseDouble(strings[1]);
        double y = Double.parseDouble(strings[2]);
        double z = Double.parseDouble(strings[3]);
        double x2 = Double.parseDouble(strings[4]);
        double y2 = Double.parseDouble(strings[5]);
        double z2 = Double.parseDouble(strings[6]);
        for(double i = x; i <= x2; i++) {
            for(double j = y; j <= y2; j++) {
                for(double k = z; k <= z2; k++) {
                    Util.sendConsoleCommand("sudo "+commandSender.getName()+" /addblock "+strings[0] + " "+ i+" "+j+" "+k);
                }
            }
        }

        return true;
    }
}
