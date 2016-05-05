package me.robomwm.PlayerCompass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Robo on 4/24/2016.
 */
public class PlayerCompass extends JavaPlugin implements Listener
{
    HashMap<Player, Player> trackingPlayers = new HashMap<Player, Player>();
    HashMap<Player, HashSet<Player>> allowedPlayers = new HashMap<Player, HashSet<Player>>();
    String compassHelp = ("\u00A7e--------- PlayerCompass -------------------------" +
            "\n\u00A76/compass allow \u00A7e\u00A7oplayer\u00A7r - Allows \u00A7e\u00A7oplayer\u00A7r to track you." +
            "\n\u00A76/compass disallow\u00A7r - Prevents all players from tracking you" +
            "\n\u00A76/compass track \u00A7e\u00A7oplayer\u00A7r - Sets your compass to track \u00A7e\u00A7oplayer" +
            "\n\u00A76/compass reset\u00A7r - Resets compass to your respawn point.");

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        final Player player = (Player)sender;

        if (cmd.getName().equalsIgnoreCase("compass"))
        {
            if (args.length < 1) //sends /compass with no args
            {
                player.sendMessage(compassHelp);
                return true;
            }

            else if (args.length > 1 && args[0].equals("allow"))
            {
                Player allowee = Bukkit.getPlayerExact(args[1]);
                //Is allowee online/valid
                if (allowee == null)
                {
                    player.sendMessage(ChatColor.RED + "Doesn't look like " + ChatColor.BLUE + args[1] + ChatColor.RED + " is online or a valid name.");
                    return true;
                }
                //If player hasn't allowed anyone before, add to hashmap
                if (!allowedPlayers.containsKey(player))
                    allowedPlayers.put(player, new HashSet<Player>());
                    //otherwise first check if they already allowed the allowee
                else if (allowedPlayers.get(player).contains(allowee))
                {
                    player.sendMessage(ChatColor.GREEN + "You already allowed " + allowee.getName() + " to track you.");
                    return true;
                }

                allowedPlayers.get(player).add(allowee);
                player.sendMessage(ChatColor.GREEN + "You allowed " + allowee.getName() + " to track you.");
                return true;
            }

            else if (args.length > 1 && args[0].toLowerCase().equals("track"))
            {
                //First check if player is holding a compass
                if (!(player.getInventory().getItemInMainHand().getType().equals(Material.COMPASS) || player.getInventory().getItemInOffHand().getType().equals(Material.COMPASS)))
                {
                    player.sendMessage(ChatColor.RED + "You need to be holding a compass in your hand to track a player");
                    return true;
                }

                final Player target = Bukkit.getPlayerExact(args[1]);
                //Check if target is invalid or invisible player
                if (target == null || !player.canSee(target))
                {
                    player.sendMessage(ChatColor.RED + "Doesn't look like " + ChatColor.AQUA + args[1] + ChatColor.RED + " is online or a valid name.");
                    return true;
                }

                //Don't allow tracking self
                if (target == player)
                {
                    player.sendMessage(ChatColor.RED + "You know where you are, right?");
                    return true;
                }

                //Check if target isn't allowing player
                if (!allowedPlayers.containsKey(target) || !allowedPlayers.get(target).equals(player))
                {
                    player.sendMessage(ChatColor.BLUE + target.getName() + ChatColor.RED + " has not allowed you to track them.");
                    player.sendMessage(ChatColor.BLUE + target.getName() + ChatColor.RED + " needs to run" + ChatColor.GOLD + " /compass allow " + player.getName() + " to allow you to track them.");
                    return true;
                }

                trackingPlayers.put(player, target);
                player.sendMessage(ChatColor.GREEN + "Your compass is now tracking " + target.getName());

                new BukkitRunnable()
                {
                    public void run()
                    {
                        //Cancel task if player is offline or is no longer tracking target
                        if (!player.isOnline() || !trackingPlayers.containsKey(player) || !trackingPlayers.get(player).equals(target))
                            this.cancel();

                            //Cancel task if target is offline
                        else if (!target.isOnline())
                        {
                            player.sendMessage(ChatColor.RED + target.getName() + " is offline. Resetting compass to your respawn point.");
                            player.setCompassTarget(player.getBedSpawnLocation());
                            this.cancel();
                        }

                        //Cancel task if target removed player from their allowedPlayers
                        else if (!trackingPlayers.containsKey(target) || !allowedPlayers.get(target).contains(player))
                        {
                            player.sendMessage(ChatColor.RED + target.getName() + " is no longer allowing you to track them. Resetting compass to your respawn point.");
                            player.setCompassTarget(player.getBedSpawnLocation());
                            this.cancel();
                        }
                        else
                            player.setCompassTarget(target.getLocation());
                    }
                }.runTaskTimer(this, 5L, 300L);
            }

            else if (args[0].toLowerCase().equals("disallow"))
            {
                if (allowedPlayers.containsKey(player))
                    allowedPlayers.remove(player);
                player.sendMessage(ChatColor.GREEN + "Prevents all players from track you");
            }

            else if (args[0].toLowerCase().equals("reset"))
            {
                player.setCompassTarget(player.getBedSpawnLocation());
                player.sendMessage(ChatColor.BLUE + "Resetting compass to your respawn point.");
                return true;
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void removeAllowedPlayersOnQuit(PlayerQuitEvent event)
    {
        if (allowedPlayers.containsKey(event.getPlayer()))
            allowedPlayers.remove(event.getPlayer());
        if (trackingPlayers.containsKey(event.getPlayer()))
        {
            trackingPlayers.remove(event.getPlayer());
            event.getPlayer().setCompassTarget(event.getPlayer().getBedSpawnLocation());
        }
    }
}
