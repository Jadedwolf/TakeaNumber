package me.olddragon.takeanumber;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class Ticket {

	/**
	 * The ticket identification number.
	 */
	private String id;

	/**
	 * Description of the ticket issue.
	 */
	public String description;

	/**
	 * When the ticket was created.
	 */
	public String dates;

	/**
	 * The player who created the ticket.
	 */
	public UUID placed_by;

	/**
	 * Location of the player when they created the ticket.
	 */
	public String location;

	/**
	 * A message from the player that created the ticket.
	 */
	public String reply;

	/**
	 * The administrator assigned to the ticket.
	 */
	public UUID admin;

	/**
	 * How the ticket was resolved.
	 */
	public String resolve;

	/**
	 * When the ticket was resolved.
	 */
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
		ticket.description = file.getString(id + ".description");
		ticket.dates = file.getString(id + ".dates");
		ticket.placed_by = UUID.fromString(file.getString(id + ".placedby"));
		ticket.location = file.getString(id + ".location", "none");
		ticket.reply = file.getString(id + ".reply", "none");
		ticket.admin = UUID.fromString(file.getString(id + ".admin"));
		ticket.resolve = file.getString(id + ".resolve", "none");
		ticket.resolved_on = file.getString(id + ".resolved_on");
		return ticket;
	}

	public void save() {
		this.source.set(this.id + ".description", this.description);
		this.source.set(this.id + ".dates", this.dates);
		this.source.set(this.id + ".placedby", this.placed_by.toString());
		this.source.set(this.id + ".location", this.location);
		this.source.set(this.id + ".reply", this.reply);
		this.source.set(this.id + ".admin", this.admin.toString());
		this.source.set(this.id + ".resolve", this.resolve);
		this.source.set(this.id + ".resolved_on", this.resolved_on);
	}

	public void toMessage(CommandSender sender) {
		Messages.sendMessage(sender, "Ticket.Heading", this.id);
		Messages.sendMessage(sender, "Ticket.Description", this.description);
		Messages.sendMessage(sender, "Ticket.Date", this.dates);
		Messages.sendMessage(sender,
		    this.location.equalsIgnoreCase("none") ? "Ticket.Location.None" : "Ticket.Location.Label", this.location);
		Messages.sendMessage(sender, "Ticket.PlacedBy", Bukkit.getOfflinePlayer(this.placed_by).getName());
		Messages.sendMessage(sender, "Ticket.Assigned", Bukkit.getOfflinePlayer(this.admin).getName());
		if (!this.reply.equals("none")) {
			Messages.sendMessage(sender, "Ticket.Reply", this.reply);
		}
		if (!this.resolve.equals("none")) {
			Messages.sendMessage(sender, "Ticket.Resolve", this.resolve);
		}
		if (this.resolved_on != null) {
			Messages.sendMessage(sender, "Ticket.ResolvedOn", this.resolved_on);
		}
	}

	public Object[] toObject() {
		Object[] values = { getId(), description, dates, placed_by, location, reply, admin, resolve, resolved_on };
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
