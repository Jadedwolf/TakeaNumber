package me.olddragon.takeanumber;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class Ticket {
  /**
   * Format for tickets
   */
  private static Pattern format = Pattern.compile("^\\d+$", Pattern.CASE_INSENSITIVE);

  private String id;

  public String description;
  public String dates;
  public String placed_by;
  public String location;
  public String reply;
  public String admin;
  public String resolve;
  public String resolved_on;
  
  private YamlConfiguration source;

  public Ticket (YamlConfiguration source, String id) {
    this.id = id;
    this.source = source;
  }
  
  public static boolean exists (YamlConfiguration file, String id) {
    return file.contains(id);
  }
  
  public static Ticket load (YamlConfiguration file, String id) {
    if (!exists(file, id)) { return null; }
    Ticket ticket = new Ticket(file, id);
    ticket.description = file.getString(id+".description"        );
    ticket.dates       = file.getString(id+".dates"              );
    ticket.placed_by   = file.getString(id+".placedby"           );
    ticket.location    = file.getString(id+".location",    "none");
    ticket.reply       = file.getString(id+".reply",       "none");
    ticket.admin       = file.getString(id+".admin",       "none");
    ticket.resolve     = file.getString(id+".resolve",     "none");
    ticket.resolved_on = file.getString(id+".resolved_on"        );
    return ticket;
  }
  
  public static List<Ticket> load (YamlConfiguration file, List<String> ids) {
    List<Ticket> result = new ArrayList<Ticket>(ids.size());
    for (String id : ids) { result.add(Ticket.load(file, id)); }
    return result;
  }
  
  public void save () {
    this.source.set(this.id+".description", this.description);
    this.source.set(this.id+".dates",       this.dates);
    this.source.set(this.id+".placedby",    this.placed_by);
    this.source.set(this.id+".location",    this.location);
    this.source.set(this.id+".reply",       this.reply);
    this.source.set(this.id+".admin",       this.admin);
    this.source.set(this.id+".resolve",     this.resolve);
    this.source.set(this.id+".resolved_on", this.resolved_on);
  }
  
  public void toMessage (CommandSender sender) {
    sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Ticket " + this.id + ChatColor.GOLD + " --");
    sender.sendMessage(" " + ChatColor.BLUE + "Ticket: " + ChatColor.RED + this.description);
    sender.sendMessage(" " + ChatColor.BLUE + "Date: " + ChatColor.WHITE + this.dates);
    sender.sendMessage(" " + ChatColor.BLUE + "Location: " + ChatColor.WHITE + (this.location.equalsIgnoreCase("none") ? "None [Console Ticket]" : this.location));
    sender.sendMessage(" " + ChatColor.BLUE + "Placed By: " + ChatColor.WHITE + this.placed_by);
    sender.sendMessage(" " + ChatColor.BLUE + "Assigned: " + ChatColor.WHITE + this.admin);
    if (!this.reply.equals("none")) { sender.sendMessage(" " + ChatColor.BLUE + "Reply: " + ChatColor.YELLOW + this.reply); }
    if (!this.resolve.equals("none")) { sender.sendMessage(" " + ChatColor.BLUE + "Resolve: " + ChatColor.GREEN + this.resolve); }
    if (this.resolved_on != null) { sender.sendMessage(" " + ChatColor.BLUE + "Resolved On: " + ChatColor.GREEN + this.resolved_on); }
  }

  /**
   * Checks to see if a string represents a ticket id
   * @param str string to check
   * @return true if the string matches the ticket format
   */
  public static boolean checkId (String str) {
    return Ticket.format.matcher(str).matches();
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  
  public YamlConfiguration getSource() { return this.source; }
  public void setSource(YamlConfiguration source) { this.source = source; }
}

