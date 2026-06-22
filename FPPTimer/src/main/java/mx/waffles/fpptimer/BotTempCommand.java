package mx.waffles.fpptimer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BotDespawnCommand implements CommandExecutor, TabCompleter {

    private final FPPTimer plugin;
    private final Map<String, BukkitTask> timers = new HashMap<>();

    public BotDespawnCommand(FPPTimer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /botdespawn <nombre|all> <tiempo|cancel>
        // Ejemplos:
        //   /botdespawn GranjaDeMobs 30m
        //   /botdespawn all 8h
        //   /botdespawn GranjaDeMobs cancel

        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        String botName = args[0];
        String tiempoArg = args[1];

        // Cancelar timer
        if (tiempoArg.equalsIgnoreCase("cancel")) {
            if (timers.containsKey(botName)) {
                timers.get(botName).cancel();
                timers.remove(botName);
                sender.sendMessage(ChatColor.RED + "✖ Timer de " + botName + " cancelado.");
            } else {
                sender.sendMessage(ChatColor.GRAY + "No hay timer activo para " + botName + ".");
            }
            return true;
        }

        // Parsear tiempo (1m, 30m, 2h, 8h, etc.)
        long ticks = parseTiempo(tiempoArg);
        if (ticks <= 0) {
            sender.sendMessage(ChatColor.RED + "Tiempo inválido. Usa formato: 30m, 2h, 8h, 1m, etc.");
            return true;
        }

        // Cancelar timer previo si existe para ese bot
        if (timers.containsKey(botName)) {
            timers.get(botName).cancel();
            timers.remove(botName);
            sender.sendMessage(ChatColor.GRAY + "Timer anterior de " + botName + " cancelado.");
        }

        String tiempoStr = formatTiempo(tiempoArg);
        sender.sendMessage(ChatColor.GREEN + "⏱ " + ChatColor.WHITE + botName
                + ChatColor.GREEN + " se despawneará en " + ChatColor.YELLOW + tiempoStr + ChatColor.GREEN + ".");

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            timers.remove(botName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fpp despawn " + botName);
            // Notificar a todos los ops
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (p.isOp()) {
                    p.sendMessage(ChatColor.RED + "⏱ [FPPTimer] Bot " + ChatColor.WHITE + botName
                            + ChatColor.RED + " despawneado después de " + tiempoStr + ".");
                }
            });
        }, ticks);

        timers.put(botName, task);
        return true;
    }

    private long parseTiempo(String input) {
        try {
            if (input.endsWith("m")) {
                int minutos = Integer.parseInt(input.replace("m", ""));
                return (long) minutos * 60 * 20;
            } else if (input.endsWith("h")) {
                int horas = Integer.parseInt(input.replace("h", ""));
                return (long) horas * 60 * 60 * 20;
            } else if (input.endsWith("s")) {
                int segundos = Integer.parseInt(input.replace("s", ""));
                return (long) segundos * 20;
            }
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    private String formatTiempo(String input) {
        if (input.endsWith("m")) return input.replace("m", "") + " minuto(s)";
        if (input.endsWith("h")) return input.replace("h", "") + " hora(s)";
        if (input.endsWith("s")) return input.replace("s", "") + " segundo(s)";
        return input;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "━━━ FPPTimer ━━━");
        sender.sendMessage(ChatColor.YELLOW + "/botdespawn <nombre|all> <tiempo>");
        sender.sendMessage(ChatColor.YELLOW + "/botdespawn <nombre|all> cancel");
        sender.sendMessage(ChatColor.GOLD + "Ejemplos:");
        sender.sendMessage(ChatColor.WHITE + "  /botdespawn GranjaDeMobs 30m");
        sender.sendMessage(ChatColor.WHITE + "  /botdespawn all 8h");
        sender.sendMessage(ChatColor.WHITE + "  /botdespawn GranjaDeMobs cancel");
        sender.sendMessage(ChatColor.GRAY + "Tiempos: 30s · 10m · 2h");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("all", "GranjaDeMobs", "MiBotAFK");
        }
        if (args.length == 2) {
            return Arrays.asList("30s", "1m", "10m", "30m", "1h", "8h", "cancel");
        }
        return Collections.emptyList();
    }
}
