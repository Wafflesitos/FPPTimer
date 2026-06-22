package mx.waffles.fpptimer;

import org.bukkit.plugin.java.JavaPlugin;

public class FPPTimer extends JavaPlugin {

    @Override
    public void onEnable() {
        BotTempCommand cmd = new BotTempCommand(this);
        getCommand("bottemp").setExecutor(cmd);
        getCommand("bottemp").setTabCompleter(cmd);
        getLogger().info("FPPTimer activado correctamente.");
    }

    @Override
    public void onDisable() {
        getLogger().info("FPPTimer desactivado.");
    }
}
