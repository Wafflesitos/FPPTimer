package mx.waffles.fpptimer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BotTempCommand implements CommandExecutor, TabCompleter {

    private final FPPTimer plugin;
    private final Map<UUID, BukkitTask> timers = new HashMap<>();

    public BotTempCommand(FPPTimer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            // Soporte desde consola también
            sender.sendMessage("[FPPTimer] Solo jugadores pueden usar /bottemp.");
            return true;
        }

        UUID uid = player.getUniqueId();

        // /bottemp cancel
        if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
            if (timers.containsKey(uid)) {
                timers.get(uid).cancel();
                timers.remove(uid);
                despawnAll();
                player.sendMessage(ChatColor.RED + "✖ Timer cancelado. Bots eliminados.");
            } else {
                player.sendMessage(ChatColor.GRAY + "No tienes un timer activo.");
            }
            return true;
        }

        // /bottemp <minutos> [cantidad]
        if (args.length < 1) {
            sendHelp(player);
            return true;
        }

        int minutos;
        try {
            minutos = Integer.parseInt(args[0]);
            if (minutos < 1 || minutos > 1440) {
                player.sendMessage(ChatColor.RED + "Los minutos deben estar entre 1 y 1440 (24h).");
                return true;
            }
        } catch (NumberFormatException e) {
            sendHelp(player);
            return true;
        }

        int cantidad = 1;
        if (args.length >= 2) {
            try {
                cantidad = Integer.parseInt(args[1]);
                if (cantidad < 1 || cantidad > 20) {
                    player.sendMessage(ChatColor.RED + "La cantidad debe estar entre 1 y 20.");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Cantidad inválida. Usa un número entre 1 y 20.");
                return true;
            }
        }

        // Cancelar timer previo si existe
        if (timers.containsKey(uid)) {
            timers.get(uid).cancel();
            timers.remove(uid);
            despawnAll();
            player.sendMessage(ChatColor.GRAY + "Timer anterior cancelado. Spawneando nuevos bots...");
        }

        // Spawnear bots
        final int finalCantidad = cantidad;
        if (cantidad == 1) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fpp spawn");
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fpp spawn " + cantidad);
        }

        // Formatear tiempo para el mensaje
        String tiempoStr = minutos == 1 ? "1 minuto" : minutos + " minutos";

        player.sendMessage(ChatColor.GREEN + "✔ " + finalCantidad + " bot(s) spawneado(s).");
        player.sendMessage(ChatColor.YELLOW + "⏱ Se eliminarán automáticamente en " + ChatColor.WHITE + tiempoStr + ChatColor.YELLOW + ".");
        player.sendMessage(ChatColor.GRAY + "Usa /bottemp cancel para eliminarlos ahora.");

        // Programar despawn
        long ticks = (long) minutos * 60L * 20L;
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            timers.remove(uid);
            despawnAll();
            Player online = Bukkit.getPlayer(uid);
            if (online != null && online.isOnline()) {
                online.sendMessage(ChatColor.RED + "⏱ Tiempo cumplido (" + tiempoStr + "). "
                        + finalCantidad + " bot(s) eliminados.");
            }
        }, ticks);

        timers.put(uid, task);
        return true;
    }

    private void despawnAll() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fpp despawn all");
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "━━━ FPPTimer ━━━");
        player.sendMessage(ChatColor.YELLOW + "/bottemp <minutos> [cantidad]");
        player.sendMessage(ChatColor.GRAY + "  Spawnea bots que se auto-eliminan.");
        player.sendMessage(ChatColor.YELLOW + "/bottemp cancel");
        player.sendMessage(ChatColor.GRAY + "  Cancela el timer y elimina los bots ya.");
        player.sendMessage(ChatColor.GOLD + "Ejemplos:");
        player.sendMessage(ChatColor.WHITE + "  /bottemp 10      " + ChatColor.GRAY + "→ 1 bot por 10 min");
        player.sendMessage(ChatColor.WHITE + "  /bottemp 30 5    " + ChatColor.GRAY + "→ 5 bots por 30 min");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("5", "10", "15", "30", "60", "cancel");
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("cancel")) {
            return Arrays.asList("1", "2", "3", "5", "10");
        }
        return Collections.emptyList();
    }
}
