package me.olddragon.takeanumber;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Stores information about a player.
 */
public class State {
  /** reference to the player for sending messages */
  public CommandSender sender;
  /** reference to the player */
  public Player player;
  /** name of the player */
  public String name;
  /** is this from the console */
  public boolean isConsole;
  /** is this an administrator */
  public boolean isAdmin;
}