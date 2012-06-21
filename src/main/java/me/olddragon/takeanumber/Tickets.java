/**
 * 
 */
package me.olddragon.takeanumber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;


public class Tickets {
  private File data_folder = null;
  private InputStream defaults = null;
  
  private YamlConfiguration config = null;
  private File file = null;
  
  public Tickets (File data_folder) {
    this.data_folder = data_folder;
    this.load();
  }
  public Tickets (File data_folder, InputStream defaults) {
    this.data_folder = data_folder;
    this.defaults = defaults;
    this.load();
  }
  
  public void initialize() throws Exception {
    FileConfigurationOptions options = this.get().options();
    options.copyDefaults(true);
    options.copyHeader(true);
    this.save();
  }
  public void load() {
    if (this.file == null) { this.file = new File(data_folder, "Tickets.yml"); }
    this.config = YamlConfiguration.loadConfiguration(this.file);;
    if (this.defaults != null) { this.config.setDefaults(YamlConfiguration.loadConfiguration(defaults)); }
  }
  public YamlConfiguration get() {
    return config;
  }
  public void save() throws Exception {
    if (config == null || file == null) {
      throw new Exception("Must load a configuration before saving.");
    }
    try {
      config.save(file);
    } catch (IOException ex) {
      throw new Exception("Could not save config to " + file.toString(), ex);
    }
  }
  
  /**
   * Add a ticket and increment the users ticket count
   * @param id
   * @param user
   */
  public void create (String id, String user) {
    java.util.List<String> Tickets = this.get().getStringList("Tickets");
    Tickets.add(id);
    this.get().set("Tickets", Tickets);
    this.get().set("counts."+user, this.get().getInt("counts."+user, 0) + 1);
  }
  
  public Ticket get (String id) { return Ticket.load(this.config, id); }
  
  /**
   * Get the tickets that are "open".
   * @return open tickets
   */
  public Collection<Ticket> getOpen() {
    Predicate<Ticket> isOpen = new Predicate<Ticket>() {
      public boolean apply(Ticket ticket) { return ticket.resolve.equals("none"); }
    };
    List<String> ids = this.get().getStringList("Tickets");
    List<Ticket> tickets = Ticket.load(this.get(), ids);
    return Util.filter(tickets, isOpen);
  }
  
  public Collection<Ticket> getExpired (final int days) {
    if (days < 1) { return new ArrayList<Ticket>(); }
    
    final Date expiration = new Date(System.currentTimeMillis() - (days * TakeaNumber.DAY_IN_MS));
    Predicate<Ticket> isExpired = new Predicate<Ticket>() {
      public boolean apply(Ticket ticket) {
        return ticket.resolved_on != null && Util.parseDate(ticket.resolved_on).before(expiration);
      }
    };
    
    List<Ticket> tickets = Ticket.load(this.get(), this.get().getStringList("Tickets"));
    return Util.filter(tickets, isExpired);
  }
  
  public void delete(String ticket) throws Exception {
    String user = "counts." + Ticket.load(this.config, ticket).placed_by;
    int count = this.config.getInt(user) - 1;
    this.config.set(user, count < 1 ? null : count);
    
    java.util.List<String> Tickets = this.config.getStringList("Tickets");
    Tickets.remove(ticket);
    this.config.set("Tickets", Tickets);
    this.config.set(ticket, null);
    
    this.save();
  }
}
