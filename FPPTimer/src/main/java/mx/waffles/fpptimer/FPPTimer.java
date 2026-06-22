package mx.waffles.fpptimer;

import org.bukkit.plugin.java.JavaPlugin;

public class FPPTimer extends JavaPlugin {

    @Override
    public void onEnable() {
        BotDespawnCommand cmd = new BotDespawnCommand(this);
        getCommand("botdespawn").setExecutor(cmd);
        getCommand("botdespawn").setTabCompleter(cmd);
        getLogger().info("FPPTimer activado.");
    }

    @Override
    public void onDisable() {
        getLogger().info("FPPTimer desactivado.");
    }
}
