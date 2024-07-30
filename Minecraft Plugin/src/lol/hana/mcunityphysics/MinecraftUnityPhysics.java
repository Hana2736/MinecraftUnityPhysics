package lol.hana.mcunityphysics;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class MinecraftUnityPhysics extends JavaPlugin {
    @Override
    public void onEnable() {
        Objects.requireNonNull(this.getCommand("loadscene")).setExecutor(new SetupCmd());
        Objects.requireNonNull(this.getCommand("addblock")).setExecutor(new AddPhysObj());
        Objects.requireNonNull(this.getCommand("fillblock")).setExecutor(new FillPhys());
        Objects.requireNonNull(this.getCommand("setmatrix")).setExecutor(new SetMatrixCmd());
        getServer().getPluginManager().registerEvents(new EventListeners(), this);
    }
}
