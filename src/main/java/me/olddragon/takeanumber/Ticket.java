package me.olddragon.takeanumber;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class Ticket {
  private String id;
  
  public String description;
  public String dates;
  public String placed_by;
  public String location;
  public String reply;
  public String admin;
  
  private FileConfiguration source;

  public Ticket (FileConfiguration source, String id) {
    this.id = id;
    this.source = source;
  }
  
  public static boolean exists (FileConfiguration file, String id) {
    return file.contains(id);
  }
  
  public static Ticket load (FileConfiguration file, String id) {
    if (!exists(file, id)) { return null; }
    Ticket ticket = new Ticket(file, id);
    ticket.description = file.getString(id+".description");
    ticket.dates       = file.getString(id+".dates");
    ticket.placed_by   = file.getString(id+".placedby");
    ticket.location    = file.getString(id+".location");
    ticket.reply       = file.getString(id+".reply");
    ticket.admin       = file.getString(id+".admin");
    return ticket;
  }
  
  public void save () {
    this.getSource().set(this.getId()+".description", this.description);
    this.getSource().set(this.getId()+".dates",       this.dates);
    this.getSource().set(this.getId()+".placedby",    this.placed_by);
    this.getSource().set(this.getId()+".location",    this.location);
    this.getSource().set(this.getId()+".reply",       this.reply);
    this.getSource().set(this.getId()+".admin",       this.admin);
  }
  
  public void toMessage (CommandSender sender) {
    sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Ticket " + this.getId() + ChatColor.GOLD + " --");
    sender.sendMessage(" " + ChatColor.BLUE + "Placed By: " + ChatColor.WHITE + this.placed_by);
    sender.sendMessage(" " + ChatColor.BLUE + "Date: " + ChatColor.WHITE + this.dates);
    sender.sendMessage(" " + ChatColor.BLUE + "Location: " + ChatColor.RED + (this.location.equalsIgnoreCase("none") ? "None [Console Ticket]" : this.location));
    sender.sendMessage(" " + ChatColor.BLUE + "Assigned Admin: " + ChatColor.WHITE + this.admin);
    sender.sendMessage(" " + ChatColor.BLUE + "Ticket: " + ChatColor.GREEN + this.description);
    sender.sendMessage(" " + ChatColor.BLUE + "Reply: " + ChatColor.YELLOW + this.reply);  
  }

  /**
   * @return the source
   */
  public FileConfiguration getSource() {
    return source;
  }

  /**
   * @param source the source to set
   */
  public void setSource(FileConfiguration source) {
    this.source = source;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

}

