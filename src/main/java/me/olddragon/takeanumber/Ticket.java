package me.olddragon.takeanumber;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class Ticket {
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

  public Ticket(YamlConfiguration source, String id) {
    this.id = id;
    this.source = source;
  }

  public static boolean exists(YamlConfiguration file, String id) {
    return file.contains(id);
  }

  public static Ticket load(YamlConfiguration file, String id) {
    if (!exists(file, id)) {
      return null;
    }
    Ticket ticket = new Ticket(file, id);
    ticket.description = file.getString(id + ".description"); //$NON-NLS-1$
    ticket.dates = file.getString(id + ".dates"); //$NON-NLS-1$
    ticket.placed_by = file.getString(id + ".placedby"); //$NON-NLS-1$
    ticket.location = file.getString(id + ".location", "none"); //$NON-NLS-1$ //$NON-NLS-2$
    ticket.reply = file.getString(id + ".reply", "none"); //$NON-NLS-1$ //$NON-NLS-2$
    ticket.admin = file.getString(id + ".admin", "none"); //$NON-NLS-1$ //$NON-NLS-2$
    ticket.resolve = file.getString(id + ".resolve", "none"); //$NON-NLS-1$ //$NON-NLS-2$
    ticket.resolved_on = file.getString(id + ".resolved_on"); //$NON-NLS-1$
    return ticket;
  }

  public void save() {
    this.source.set(this.id + ".description", this.description); //$NON-NLS-1$
    this.source.set(this.id + ".dates", this.dates); //$NON-NLS-1$
    this.source.set(this.id + ".placedby", this.placed_by); //$NON-NLS-1$
    this.source.set(this.id + ".location", this.location); //$NON-NLS-1$
    this.source.set(this.id + ".reply", this.reply); //$NON-NLS-1$
    this.source.set(this.id + ".admin", this.admin); //$NON-NLS-1$
    this.source.set(this.id + ".resolve", this.resolve); //$NON-NLS-1$
    this.source.set(this.id + ".resolved_on", this.resolved_on); //$NON-NLS-1$
  }

  public void toMessage(CommandSender sender) {
    Messages.sendMessage(sender, "Ticket.Heading", this.id); //$NON-NLS-1$
    Messages.sendMessage(sender, "Ticket.Description", this.description); //$NON-NLS-1$
    Messages.sendMessage(sender, "Ticket.Date", this.dates); //$NON-NLS-1$
    Messages.sendMessage(sender, "Ticket.Location", (this.location.equalsIgnoreCase("none") ? Messages.getString("Ticket.Location.None") : this.location)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    Messages.sendMessage(sender, "Ticket.PlacedBy", this.placed_by); //$NON-NLS-1$
    Messages.sendMessage(sender, "Ticket.Assigned", this.admin); //$NON-NLS-1$
    if (!this.reply.equals("none")) { //$NON-NLS-1$
      Messages.sendMessage(sender, "Ticket.Reply", this.reply); //$NON-NLS-1$
    }
    if (!this.resolve.equals("none")) { //$NON-NLS-1$
      Messages.sendMessage(sender, "Ticket.Resolve", this.resolve); //$NON-NLS-1$
    }
    if (this.resolved_on != null) {
      Messages.sendMessage(sender, "Ticket.ResolvedOn", this.resolved_on); //$NON-NLS-1$
    }
  }
  
  public Object[] toObject() {
    Object[] values = {
      getId(),
      description,
      dates,
      placed_by,
      location,
      reply,
      admin,
      resolve,
      resolved_on
    };
    return values;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public YamlConfiguration getSource() {
    return this.source;
  }

  public void setSource(YamlConfiguration source) {
    this.source = source;
  }
}
