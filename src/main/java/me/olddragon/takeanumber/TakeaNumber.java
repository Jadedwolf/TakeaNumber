package me.olddragon.takeanumber;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TakeaNumber extends JavaPlugin implements Listener {

  private Tickets tickets = null;

  @Override
  public void onEnable(){
    // Load configuration
    FileConfigurationOptions cfgOptions = getConfig().options();
    cfgOptions.copyDefaults(true);
    cfgOptions.copyHeader(true);
    saveConfig();

    String format = getConfig().getString("DateFormat");
    if (format != null) {
      try {
        Util.setDateFormat(format);
      } catch (IllegalArgumentException ex) {
        this.getLogger().log(Level.WARNING, getConfig().getString("Messages.InvalidDateFormat"), format);
      }
    }
    
    tickets = new Tickets(getDataFolder(), getResource("Tickets.yml"));
    try { this.tickets.initialize(); } catch(Exception ex) { this.getLogger().severe(ex.toString()); }
    this.tickets.getExpired(0);

    // Register the event listeners
    this.getServer().getPluginManager().registerEvents(this, this);

    this.deleteExpiredTickets();
  }
  
  /**
   * When a player joins, show the number of open tickets if they are an admin.
   * @param event
   */
  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!this.getConfig().getBoolean("ShowTicketsOnJoin")) { return; }
    
    Player player = event.getPlayer();
    if (player != null && player.hasPermission("tan.admin")) {
      if (this.getConfig().getBoolean("AlwaysLoadTickets", false)) { this.tickets.load(); }
      Util.sendMessage(player, getConfig().getString("Messages.OpenTickets"), this.tickets.getOpen().size());
    }
  }

  public class State {
    public CommandSender sender;
    public Player player;
    public String name;
    public boolean isConsole;
    public boolean isAdmin;
  }

  /**
   * Display the list of commands
   * @param sender Who to send the list too
   * @param isAdmin Show the administrator commands
   */
  protected void usage (State state) {
    List<String> lines = this.getConfig().getStringList("Messages.PlayerHelp");
    for(String line : lines) { state.sender.sendMessage(line); }
    if(state.isAdmin) {
      lines = this.getConfig().getStringList("Messages.AdminHelp");
      for(String line : lines) { state.sender.sendMessage(line); }
    }
  }

  @Override
  public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
    String command = cmd.getName().toLowerCase();

    State state = new State();
    state.sender = sender;
    state.player = sender instanceof Player ? (Player) sender : null;
    state.name = state.player == null ? "" : state.player.getDisplayName();
    state.isConsole = state.player == null;
    state.isAdmin = state.player == null || state.player.hasPermission("tan.admin");

    if (getConfig().getBoolean("AlwaysLoadTickets", false)) { this.tickets.load(); }

    if      (command.equals("ticket-help")    && args.length == 0) { usage(state);            }
    else if (command.equals("ticket-list")    && args.length == 0) { cmdList(state, args);    }
    else if (command.equals("ticket-open")    && args.length >  0) { cmdOpen(state, args);    }
    else if (command.equals("ticket-check")   && args.length == 1) { cmdCheck(state, args);   }
    else if (command.equals("ticket-take")    && args.length == 1) { cmdTake(state, args);    }
    else if (command.equals("ticket-visit")   && args.length == 1) { cmdVisit(state, args);   }
    else if (command.equals("ticket-reply")   && args.length >  1) { cmdReply(state, args);   }
    else if (command.equals("ticket-resolve") && args.length >  1) { cmdResolve(state, args); }
    else if (command.equals("ticket-delete")  && args.length == 1) { cmdDelete(state, args);  }
    else { usage(state); }

    return true;
  }

  /**
   * Get a tickets information
   * @param state
   * @param args
   */
  private void cmdCheck(State state, String[] args) {
    String id = args[0];
    if (!Ticket.checkId(id)) { Util.sendMessage(state.sender, getMessage("InvalidTicketFormat"), id); return; }
    Ticket ticket = this.tickets.get(id);

    if (ticket == null) {
      Util.sendMessage(state.sender, getMessage("InvalidTicketNumber"), id);
    } else if (!state.isAdmin && !ticket.placed_by.equals(state.name)) {
      Util.sendMessage(state.sender, getMessage("NotYourTicket"), id);
    } else {
      ticket.toMessage(state.sender);
    }
  }

  /**
   * Delete a ticket
   * @param state
   * @param args
   */
  private void cmdDelete(State state, String[] args) {
    String id = args[0];
    if (!Ticket.checkId(id)) { Util.sendMessage(state.sender, getMessage("InvalidTicketFormat"), id); return; }
    Ticket ticket = Ticket.load(this.tickets.get(), id);
    if (ticket == null) { Util.sendMessage(state.sender, getMessage("InvalidTicketNumber"), id); return; }
    if (!state.isAdmin && !ticket.placed_by.equals(state.name)) { Util.sendMessage(state.sender, getMessage("NotYourTicket"), id); return; }

    try { this.tickets.delete(id); } catch(Exception ex) { this.getLogger().severe(ex.toString()); }
    Util.sendMessage(state.sender, this.getConfig().getString("Messages.DeleteTicket"), id);
    if (state.isAdmin) {
      String admin = state.isConsole ? "(Console)" : state.name;

      Player target = getServer().getPlayer(ticket.placed_by);
      Util.sendMessage(target, getMessage("AdminDeleteTicket"), new Object[] { admin, id });

      notifyAdmins(getMessage("AdminDeleteTicket"), new Object[] { admin, id });
    } else {
      notifyAdmins(getMessage("PlayerDeleteTicket"), new Object[] { state.name, id });
    }
  }

  /**
   * List the tickets
   * @param state
   * @param args
   */
  private void cmdList(State state, String[] args) {
    java.util.List<String> Tickets = this.tickets.get().getStringList("Tickets");
    if (Tickets.isEmpty()) {
      Util.sendMessage(state.sender, getMessage("NoTickets"));
    } else {
      Util.sendMessage(state.sender, getMessage("ListHeading"));
      for (String id : Tickets) {
        Ticket ticket = Ticket.load(this.tickets.get(), id);
        if (ticket != null && (state.isAdmin || ticket.placed_by.equals(state.name))) {
          ChatColor color =
            !ticket.reply.equals("none") ? ChatColor.YELLOW :
            !ticket.resolve.equals("none") ? ChatColor.GREEN :
            ChatColor.RED;
          Util.sendMessage(state.sender,
            this.getConfig().getString("Messages.ListEntry"),
            new Object[] { color, ticket.getId(), ticket.placed_by, ticket.description, (ticket.location.equals("none") ? "" : " @ " + ticket.location) }
          );
        }
      }
    }
  }

  /**
   * Open a new ticket
   * @param state
   * @param args
   */
  private void cmdOpen(State state, String[] args) {
    if (state.player != null) {
      int count = this.tickets.get().getInt("counts." + state.name, 0);
      int MaxTickets = getConfig().getInt("MaxTickets");
      if (count >= MaxTickets) { Util.sendMessage(state.sender, getMessage("TicketLimit")); return; }
    }

    java.util.List<String> tickets = this.tickets.get().getStringList("Tickets");
    String next_ticket = String.valueOf(tickets.isEmpty() ? 0 : Integer.parseInt(Ticket.load(this.tickets.get(), tickets.get(tickets.size() - 1)).getId(), 10) + 1);
    Ticket ticket = new Ticket(this.tickets.get(), next_ticket);

    StringBuilder message = new StringBuilder();
    for (int i=0; i<args.length; i++) { message.append(args[i]).append(" "); }

    ticket.description = message.toString();
    ticket.dates = Util.getCurrentDate();

    if (state.isConsole) {
      this.tickets.create(next_ticket, "Console");
      ticket.placed_by = "Console";
    } else {
      this.tickets.create(next_ticket, state.player.getDisplayName());
      ticket.placed_by = state.player.getDisplayName();
      ticket.location = String.format("%s,%d,%d,%d",
        state.player.getWorld().getName(),
        (int)state.player.getLocation().getX(),
        (int)state.player.getLocation().getY(),
        (int)state.player.getLocation().getZ()
      );
    }

    ticket.save();
    try { this.tickets.save(); } catch(Exception ex) { this.getLogger().severe(ex.toString()); }
    
    Util.sendMessage(state.sender, getMessage("OpenedTicket"), ticket.getId());
    notifyAdmins(getMessage("OpenedTicket"), new Object[] { ticket.getId(), state.isConsole ? "Console" : state.name });
  }

  /**
   * Reply to a ticket
   * @param state
   * @param args
   */
  private void cmdReply(State state, String[] args) {
    String id = args[0];
    if (!Ticket.checkId(id)) { Util.sendMessage(state.sender, getMessage("InvalidTicketFormat"), id); return; }
    Ticket ticket = Ticket.load(this.tickets.get(), id);
    if (ticket == null) { Util.sendMessage(state.sender, getMessage("InvalidTicketNumber"), id); return; }
    if (!state.isAdmin && !ticket.placed_by.equals(state.name)) { Util.sendMessage(state.sender, getMessage("NotYourTicket"), id); return; }

    StringBuilder message = new StringBuilder();
    for (int i=1; i<args.length; i++) { message.append(args[i]).append(" "); }

    ticket.reply = (state.isConsole ? "(Console) " : "(" + state.name + ") ") + message.toString();
    ticket.save();
    try { this.tickets.save(); } catch(Exception ex) { this.getLogger().severe(ex.toString()); }

    Util.sendMessage(state.sender, getMessage("ReplyTicket"), id);
    
    if (state.name.equals(ticket.placed_by)) {
      Player target = getServer().getPlayer(ticket.admin);
      Util.sendMessage(target, getMessage("PlayerReply"), state.name);
    } else if (state.isAdmin) {
      Player target = getServer().getPlayer(ticket.placed_by);
      Util.sendMessage(target, getMessage("AdminReply"), state.isConsole ? "(Console)" : state.name);
    }
  }

  /**
   * Close a ticket
   * @param state
   * @param args
   */
  private void cmdResolve(State state, String[] args) {
    String id = args[0];
    if (!Ticket.checkId(id)) { Util.sendMessage(state.sender, getMessage("InvalidTicketFormat"), id); return; }
    Ticket ticket = Ticket.load(this.tickets.get(), id);
    if (ticket == null) { Util.sendMessage(state.sender, getMessage("InvalidTicketNumber"), id); return; }
    if (!state.isAdmin && !ticket.placed_by.equals(state.name)) { Util.sendMessage(state.sender, getMessage("NotYourTicket"), id); return; }

    StringBuilder resolve = new StringBuilder();
    if (args.length > 1) {
      for (int i=1; i<args.length; i++) { resolve.append(args[i]).append(" "); }
    } else {
      resolve.append("resolved");
    }

    ticket.reply = "none";
    ticket.resolve = (state.isConsole ? "(Console) " : "(" + state.name + ") ") + resolve.toString();
    ticket.resolved_on = Util.getCurrentDate();
    ticket.save();
    try { this.tickets.save(); } catch(Exception ex) { this.getLogger().severe(ex.toString()); }

    Util.sendMessage(state.sender, getMessage("ResolveTicket"), id);
    
    if (state.name.equals(ticket.placed_by)) {
      Player target = getServer().getPlayer(ticket.admin);
      Util.sendMessage(target, getMessage("PlayerResolve"), state.name);
    } else if (state.isAdmin) {
      Player target = getServer().getPlayer(ticket.placed_by);
      Util.sendMessage(target, getMessage("AdminResolve"), state.isConsole ? "(Console)" : state.name);
    }
  }

  /**
   * Take a ticket from the list
   * @param state
   * @param args
   */
  private void cmdTake(State state, String[] args) {
    if (! state.isAdmin) { Util.sendMessage(state.sender, getMessage("AdminOnly")); return; }
    String id = args[0];
    if (!Ticket.checkId(id)) { Util.sendMessage(state.sender, getMessage("InvalidTicketFormat"), id); return; }
    Ticket ticket = Ticket.load(this.tickets.get(), id);
    if (ticket == null) { Util.sendMessage(state.sender, getMessage("InvalidTicketNumber"), id); return; }

    ticket.admin = state.name;
    ticket.save();
    try { this.tickets.save(); } catch(Exception ex) { this.getLogger().severe(ex.toString()); }

    ticket.toMessage(state.sender);

    Player target = getServer().getPlayer(ticket.placed_by);
    Util.sendMessage(target, getMessage("TakeTicket"), new Object[] { state.name, id });
  }

  /**
   * Teleport the player to the location from which a ticket was submitted
   * @param state
   * @param args
   */
  private void cmdVisit(State state, String[] args) {
    if (state.isConsole) { Util.sendMessage(state.sender, getMessage("NoConsole")); return; }
    if (! state.isAdmin) { Util.sendMessage(state.sender, getMessage("AdminOnly")); return; }
    String id = args[0];
    if (!Ticket.checkId(id)) { Util.sendMessage(state.sender, getMessage("InvalidTicketFormat"), id); return; }
    Ticket ticket = Ticket.load(this.tickets.get(), id);
    if (ticket == null) { Util.sendMessage(state.sender, getMessage("InvalidTicketNumber"), id); return; }

    if (state.player != null && !ticket.location.equals("none")) {
      String[] vals = ticket.location.split(",");
      World world = Bukkit.getWorld(vals[0]);
      double x = Double.parseDouble(vals[1]);
      double y = Double.parseDouble(vals[2]);
      double z = Double.parseDouble(vals[3]);
      state.player.teleport(new Location(world, x, y, z));
    }
  }


  final static long DAY_IN_MS = 1000 * 60 * 60 * 24;

  protected void deleteExpiredTickets () {
    final int days = getConfig().getInt("ResolvedTicketExpiration", 7);
    if (days == 0) { return; }
    Logger logger = this.getLogger();
    logger.info(getMessage("StartDeleteTickets"));
    Collection<Ticket> expired = this.tickets.getExpired(days);
    for (Ticket ticket : expired) { try { this.tickets.delete(ticket.getId()); } catch(Exception ex) { logger.severe(ex.toString()); } }
    logger.log(Level.INFO, getMessage("FinishDeleteTickets"), new Object[] { expired.size() });
  }
  
  protected String getMessage(String key) { return this.getConfig().getString("Messages." + key); };

  /**
   * Notify all online administrators
   * @param message message to send
   */
  protected void notifyAdmins (String message) {
    if (! getConfig().getBoolean("NotifyAdminOnTicketClose")) { return; }
    Player[] players = this.getServer().getOnlinePlayers();
    for(Player op : players) { if(op.hasPermission("tan.admin")) { op.sendMessage(message); } }
  }
  protected void notifyAdmins (String message, String variables) { notifyAdmins(String.format(message, variables)); }
  protected void notifyAdmins (String message, Object[] variables) { notifyAdmins(String.format(message, variables)); }
}
