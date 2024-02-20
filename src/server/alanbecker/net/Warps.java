package server.alanbecker.net;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Warps extends JavaPlugin implements CommandExecutor {
    @Override
    public void onEnable() {
        this.getCommand("rwarp").setExecutor(this);
        this.getCommand("setpoint").setExecutor(this);
        this.saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Only players can use this command."));
            return true;
        }

        Player player = (Player) sender;
        FileConfiguration config = this.getConfig();

        if ("rwarp".equalsIgnoreCase(label)) {
            if (args.length > 0 && "create".equalsIgnoreCase(args[0])) {
                if (!player.hasPermission("abmc.create.rwarp")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have permission to create warps."));
                    return true;
                }
                return handleCreateWarpCommand(player, args, config);
            } else {
                if (!player.hasPermission("abmc.rwarp.teleport")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have permission to teleport to warps."));
                    return true;
                }
                return handleTeleportWarpCommand(player, args, config);
            }
        } else if ("setpoint".equalsIgnoreCase(label)) {
            if (!player.hasPermission("abmc.create.setpoint")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have permission to set warp points."));
                return true;
            }
            return handleSetPointCommand(player, args, config);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Command not recognized."));
            return true;
        }
    }

    private boolean handleCreateWarpCommand(Player player, String[] args, FileConfiguration config) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Usage: /rwarp create <warpName>"));
            return true;
        }
        String warpName = args[1].toLowerCase();
        if (config.contains("warps." + warpName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Warp " + warpName + " already exists."));
        } else {
            config.set("warps." + warpName, new ArrayList<>());
            this.saveConfig();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Warp " + warpName + " created."));
        }
        return true;
    }

    private boolean handleTeleportWarpCommand(Player player, String[] args, FileConfiguration config) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Usage: /rwarp <warpName>"));
            return true;
        }

        String warpName = args[0].toLowerCase();
        String matchedWarpName = config.getConfigurationSection("warps").getKeys(false)
                .stream()
                .filter(name -> name.equalsIgnoreCase(warpName))
                .findFirst()
                .orElse(null);

        if (matchedWarpName == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Warp " + warpName + " does not exist."));
            return true;
        }

        List<String> points = config.getStringList("warps." + matchedWarpName);
        if (points.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Warp " + matchedWarpName + " has no points set."));
            return true;
        }

        String randomPoint = points.get(new Random().nextInt(points.size()));
        String[] coords = randomPoint.split(",");
        World world = Bukkit.getWorld(coords[0]);
        if (world == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7The world for warp " + matchedWarpName + " could not be found."));
            return true;
        }

        Location loc = new Location(world, Double.parseDouble(coords[1]), Double.parseDouble(coords[2]), Double.parseDouble(coords[3]), Float.parseFloat(coords[4]), Float.parseFloat(coords[5]));
        player.teleport(loc);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Warping to " + matchedWarpName));

        return true;
    }

    private boolean handleSetPointCommand(Player player, String[] args, FileConfiguration config) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Usage: /setpoint <warpName>"));
            return true;
        }

        String warpName = args[0].toLowerCase();
        if (!config.contains("warps." + warpName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Warp " + warpName + " does not exist. Use '/rwarp create " + warpName + "' to create it first."));
            return true;
        }

        List<String> points = config.getStringList("warps." + warpName);
        Location loc = player.getLocation();
        points.add(loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch());
        config.set("warps." + warpName, points);
        this.saveConfig();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Point added to warp " + warpName));

        return true;
    }
}
