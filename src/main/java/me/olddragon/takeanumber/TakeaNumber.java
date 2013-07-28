package me.olddragon.takeanumber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class TakeaNumber extends JavaPlugin {

  static final Logger log = Logger.getLogger("Minecraft"); //$NON-NLS-1$
  
  public static SimpleDateFormat date_format = null;
  private YamlConfiguration tickets_config = null;
  private File tickets_file = null;
  @SuppressWarnings("unused")
  private PListener listener = null;

  public static String getCurrentDate() {
    return date_format.format(Calendar.getInstance().getTime());
  }

  public void loadTickets() {
    if (tickets_file == null) {
      tickets_file = new File(getDataFolder(), "Tickets.yml"); //$NON-NLS-1$
    }
    tickets_config = YamlConfiguration.loadConfiguration(tickets_file);
    InputStream defaults = getResource("Tickets.yml"); //$NON-NLS-1$
    if (defaults != null) {
      tickets_config.setDefaults(YamlConfiguration.loadConfiguration(defaults));
    }
  }

  public YamlConfiguration getTickets() {
    if (tickets_config == null) {
      loadTickets();
    }
    return tickets_config;
  }

  public void saveTickets() {
    if (tickets_config == null || tickets_file == null) {
      return;
    }
    try {
      tickets_config.save(tickets_file);
    } catch (IOException ex) {
      Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, Messages.getString("Error.Config.Save", tickets_file.toString()), ex); //$NON-NLS-1$
    }
  }

  @Override
  public void onEnable() {
    // Load configuration
    FileConfigurationOptions cfgOptions = getConfig().options();
    cfgOptions.copyDefaults(true);
    cfgOptions.copyHeader(true);
    saveConfig();

    if (date_format == null) {
      String format = getConfig().getString("DateFormat"); //$NON-NLS-1$
      try {
        date_format = new SimpleDateFormat(format);
      } catch (IllegalArgumentException ex) {
        Logger.getLogger(JavaPlugin.class.getName()).log(Level.WARNING, Messages.getString("Error.Date.InvalidFormat", format), ex); //$NON-NLS-1$
        date_format = new SimpleDateFormat();
      }
    }

    // Load Tickets
    FileConfigurationOptions ticketOptions = getTickets().options();
    ticketOptions.copyDefaults(true);
    ticketOptions.copyHeader(true);
    saveTickets();

    // declare new listener
    this.listener = new PListener(this);

    log.log(Level.INFO, Messages.getString("General.Enabled"), new Object[]{getDescription().getName(), getDescription().getVersion()}); //$NON-NLS-1$
    expireTickets();
  }

  @Override
  public void onDisable() {
    log.log(Level.INFO, Messages.getString("General.Disabled", getDescription().getName(), getDescription().getVersion())); //$NON-NLS-1$
  }

  public class PListener implements Listener {

    public PListener(TakeaNumber instance) {
      Plugin plugin = instance;
      Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
      if (getConfig().getBoolean("ShowTicketsOnJoin") == true) { //$NON-NLS-1$
        Player player = event.getPlayer();
        if (player != null && player.hasPermission("tan.admin")) { //$NON-NLS-1$
          if (getConfig().getBoolean("AlwaysLoadTickets", false)) { //$NON-NLS-1$
            loadTickets();
          }
          int length = getTickets().getStringList("Tickets").size(); //$NON-NLS-1$
          if (length > 0) {
            player.sendMessage(Messages.getString("General.Login", length)); //$NON-NLS-1$
          }
        }
      }
    }
  }

  /**
   * Get the player name
   *
   * @param name
   * @return
   */
  public String getPlayerName(String name) {
    Player caddPlayer = getServer().getPlayerExact(name);
    String pName;
    if (caddPlayer == null) {
      caddPlayer = getServer().getPlayer(name);
      if (caddPlayer == null) {
        pName = name;
      } else {
        pName = caddPlayer.getName();
      }
    } else {
      pName = caddPlayer.getName();
    }
    return pName;
  }

  private static final String[] user_commands = new String[] {
    "Command.User.Heading", "Command.User.Open", "Command.User.List", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    "Command.User.Check", "Command.User.Reply", "Command.User.Resolve", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    "Command.User.Delete" //$NON-NLS-1$
  };
  private static final String[] admin_commands = new String[] {
    "Command.Admin.Heading", "Command.Admin.List", "Command.Admin.Check", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    "Command.Admin.Take", "Command.Admin.Visit", "Command.Admin.Reply", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    "Command.Admin.Resolve", "Command.Admin.Delete" //$NON-NLS-1$ //$NON-NLS-2$
  };
  /**
   * Display the list of commands
   *
   * @param sender Who to send the list too
   * @param isAdmin Show the administrator commands
   */
  protected void usage(State state) {
    for (String command : user_commands) { Messages.sendMessage(state.sender, command); }
    if (state.isAdmin) {
      for (String command : admin_commands) { Messages.sendMessage(state.sender, command); }
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    String command = cmd.getName().toLowerCase();

    State state = new State();
    state.sender = sender;
    state.player = sender instanceof Player ? (Player) sender : null;
    state.name = state.player == null ? "" : state.player.getDisplayName(); //$NON-NLS-1$
    state.isConsole = state.player == null;
    state.isAdmin = state.player == null || state.player.hasPermission("tan.admin"); //$NON-NLS-1$

    if (getConfig().getBoolean("AlwaysLoadTickets", false)) { //$NON-NLS-1$
      loadTickets();
    }

    if (command.equals("ticket-help") && args.length == 0) { //$NON-NLS-1$
      usage(state);
    } else if (command.equals("ticket-list") && args.length == 0) { //$NON-NLS-1$
      cmdList(state, args);
    } else if (command.equals("ticket-open") && args.length > 0) { //$NON-NLS-1$
      cmdOpen(state, args);
    } else if (command.equals("ticket-check") && args.length == 1) { //$NON-NLS-1$
      cmdCheck(state, args);
    } else if (command.equals("ticket-take") && args.length == 1) { //$NON-NLS-1$
      cmdTake(state, args);
    } else if (command.equals("ticket-visit") && args.length == 1) { //$NON-NLS-1$
      cmdVisit(state, args);
    } else if (command.equals("ticket-reply") && args.length > 1) { //$NON-NLS-1$
      cmdReply(state, args);
    } else if (command.equals("ticket-resolve") && args.length > 1) { //$NON-NLS-1$
      cmdResolve(state, args);
    } else if (command.equals("ticket-delete") && args.length == 1) { //$NON-NLS-1$
      cmdDelete(state, args);
    } else {
      usage(state);
    }

    return true;
  }

  /**
   * Get a tickets information
   *
   * @param state
   * @param args
   */
  private void cmdCheck(State state, String[] args) {
    String id = args[0];
    if (!isTicket(id)) {
      Messages.sendMessage(state.sender, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
      return;
    }
    Ticket ticket = Ticket.load(getTickets(), id);

    if (ticket == null) {
      Messages.sendMessage(state.sender, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
    } else if (!state.isAdmin && !ticket.placed_by.equals(state.name)) {
      Messages.sendMessage(state.sender, "Error.Ticket.NotOwner"); //$NON-NLS-1$
    } else {
      ticket.toMessage(state.sender);
    }
  }

  /**
   * Delete a ticket
   *
   * @param state
   * @param args
   */
  private void cmdDelete(State state, String[] args) {
    String id = args[0];
    if (!isTicket(id)) {
      Messages.sendMessage(state.player, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
      return;
    }
    Ticket ticket = Ticket.load(getTickets(), id);
    if (ticket == null) {
      Messages.sendMessage(state.sender, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
      return;
    }
    if (!state.isAdmin && !ticket.placed_by.equals(state.name)) {
      Messages.sendMessage(state.sender, "Error.Ticket.NotOwner"); //$NON-NLS-1$
      return;
    }

    deleteTicket(id);
    Messages.sendMessage(state.sender, "Command.Delete.Notify", id); //$NON-NLS-1$
    if (state.isAdmin) {
      String admin = state.isConsole ? Messages.getString("General.ConsoleName") : state.name; //$NON-NLS-1$
      Player target = getServer().getPlayer(ticket.placed_by);
      if (target != null) {
        Messages.sendMessage(target, "Command.Delete.Admin", id, admin); //$NON-NLS-1$
      }
      notifyAdmins(Messages.getString("Command.Delete.Admin", id, admin)); //$NON-NLS-1$
    } else {
      notifyAdmins(Messages.getString("Command.Delete.User", id, state.name)); //$NON-NLS-1$
    }
  }

  /**
   * List the tickets
   *
   * @param state
   * @param args
   */
  private void cmdList(State state, String[] args) {
    java.util.List<String> Tickets = getTickets().getStringList("Tickets"); //$NON-NLS-1$
    if (Tickets.isEmpty()) {
      Messages.sendMessage(state.sender, "Command.List.Empty"); //$NON-NLS-1$
    } else {
      Messages.sendMessage(state.sender, "Command.List.Heading"); //$NON-NLS-1$
      for (String id : Tickets) {
        Ticket ticket = Ticket.load(getTickets(), id);
        if (ticket != null && (state.isAdmin || ticket.placed_by.equals(state.name))) {
          Object[] values = ticket.toObject();
          if (!ticket.reply.equals("none")) { //$NON-NLS-1$
            Messages.sendMessage(state.sender, "Command.List.Replied", values); //$NON-NLS-1$
          } else if (!ticket.resolve.equals("none")) { //$NON-NLS-1$
            Messages.sendMessage(state.sender, "Command.List.Resolved", values); //$NON-NLS-1$
          } else {
            Messages.sendMessage(state.sender, "Command.List.Open", values); //$NON-NLS-1$
          }
        }
      }
    }
  }

  /**
   * Open a new ticket
   *
   * @param state
   * @param args
   */
  private void cmdOpen(State state, String[] args) {
    if (state.player != null && !state.isAdmin) {
      int count = getTickets().getInt("counts." + state.name, 0); //$NON-NLS-1$
      int MaxTickets = getConfig().getInt("MaxTickets"); //$NON-NLS-1$
      if (count >= MaxTickets) {
        Messages.sendMessage(state.player, "Ticket.MaxTickets", MaxTickets); //$NON-NLS-1$
        return;
      }
    }

    java.util.List<String> tickets = getTickets().getStringList("Tickets"); //$NON-NLS-1$
    String next_ticket = String.valueOf(tickets.isEmpty() ? 0 : Integer.parseInt(Ticket.load(getTickets(), tickets.get(tickets.size() - 1)).getId(), 10) + 1);
    Ticket ticket = new Ticket(getTickets(), next_ticket);

    StringBuilder message = new StringBuilder();
    for (int i = 0; i < args.length; i++) {
      message.append(args[i]).append(" "); //$NON-NLS-1$
    }

    ticket.description = message.toString();
    ticket.dates = getCurrentDate();

    if (state.isConsole) {
      newTicket(next_ticket, Messages.getString("General.ConsoleName")); //$NON-NLS-1$
      ticket.placed_by = Messages.getString("General.ConsoleName"); //$NON-NLS-1$
    } else {
      newTicket(next_ticket, state.player.getDisplayName());
      ticket.placed_by = state.player.getDisplayName();
      ticket.location = String.format("%s,%d,%d,%d", //$NON-NLS-1$
              state.player.getWorld().getName(),
              (int) state.player.getLocation().getX(),
              (int) state.player.getLocation().getY(),
              (int) state.player.getLocation().getZ());
    }

    ticket.save();
    saveTickets();

    Messages.sendMessage(state.sender, "Command.Create.User", ticket.getId()); //$NON-NLS-1$
    notifyAdmins(Messages.getString("Command.Create.Admin", (state.isConsole ? Messages.getString("General.ConsoleName") : state.name))); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Reply to a ticket
   *
   * @param state
   * @param args
   */
  private void cmdReply(State state, String[] args) {
    String id = args[0];
    if (!isTicket(id)) {
      Messages.sendMessage(state.player, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
      return;
    }
    Ticket ticket = Ticket.load(getTickets(), id);
    if (ticket == null) {
      Messages.sendMessage(state.sender, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
      return;
    }
    if (!state.isAdmin && !ticket.placed_by.equals(state.name)) {
      Messages.sendMessage(state.sender, "Error.Ticket.NotOwner"); //$NON-NLS-1$
      return;
    }

    StringBuilder message = new StringBuilder();
    for (int i = 1; i < args.length; i++) {
      message.append(args[i]).append(" "); //$NON-NLS-1$
    }

    ticket.reply = Messages.getString("Command.Reply.Format", state.isConsole ? Messages.getString("General.ConsoleName") : state.name, message.toString()); //$NON-NLS-1$ //$NON-NLS-2$
    ticket.save();
    saveTickets();

    Messages.sendMessage(state.sender, "Command.Reply.Replied", id); //$NON-NLS-1$
    if (state.name.equals(ticket.placed_by)) {
      Player target = getServer().getPlayer(ticket.admin);
      if (target != null) {
        Messages.sendMessage(target, "Command.Reply.User", state.name); //$NON-NLS-1$
      }
    } else if (state.isConsole) {
      Player target = getServer().getPlayer(ticket.placed_by);
      if (target != null) {
        Messages.sendMessage(target, "Command.Reply.Console"); //$NON-NLS-1$
      }
    } else if (state.isAdmin) {
      Player target = getServer().getPlayer(ticket.placed_by);
      if (target != null) {
        Messages.sendMessage(target, "Command.Reply.Admin", state.name); //$NON-NLS-1$
      }
    }
  }

  /**
   * Close a ticket
   *
   * @param state
   * @param args
   */
  private void cmdResolve(State state, String[] args) {
    String id = args[0];
    if (!isTicket(id)) {
      Messages.sendMessage(state.player, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
      return;
    }
    Ticket ticket = Ticket.load(getTickets(), id);
    if (ticket == null) {
      Messages.sendMessage(state.sender, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
      return;
    }
    if (!state.isAdmin && !ticket.placed_by.equals(state.name)) {
      Messages.sendMessage(state.sender, "Error.Ticket.NotOwner"); //$NON-NLS-1$
      return;
    }

    StringBuilder resolve = new StringBuilder();
    if (args.length > 1) {
      for (int i = 1; i < args.length; i++) {
        resolve.append(args[i]).append(" "); //$NON-NLS-1$
      }
    } else {
      resolve.append("resolved"); //$NON-NLS-1$
    }

    ticket.reply = "none"; //$NON-NLS-1$
    ticket.resolve = resolve.toString();
    ticket.resolved_on = TakeaNumber.getCurrentDate();
    ticket.save();
    saveTickets();

    Messages.sendMessage(state.sender, "Command.Resolve.Resolved", id); //$NON-NLS-1$
    if (state.isAdmin) {
      String admin = state.isConsole ? Messages.getString("General.ConsoleName") : state.name; //$NON-NLS-1$
      Player target = getServer().getPlayer(ticket.placed_by);
      if (target != null) {
        Messages.sendMessage(target, "Command.Resolve.Admin", admin); //$NON-NLS-1$
      }
      notifyAdmins(Messages.getString("Command.Resolve.Admin", id, admin)); //$NON-NLS-1$
    } else {
      notifyAdmins(Messages.getString("Command.Resolve.User", id, state.name)); //$NON-NLS-1$
    }
  }

  /**
   * Take a ticket from the list
   *
   * @param state
   * @param args
   */
  private void cmdTake(State state, String[] args) {
    if (!state.isAdmin) {
      Messages.sendMessage(state.player, "Error.Command.AdminOnly"); //$NON-NLS-1$
      return;
    }
    String id = args[0];
    if (!isTicket(id)) {
      Messages.sendMessage(state.player, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
      return;
    }
    Ticket ticket = Ticket.load(getTickets(), id);
    if (ticket == null) {
      Messages.sendMessage(state.sender, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
      return;
    }

    ticket.admin = state.name;
    ticket.save();
    saveTickets();

    ticket.toMessage(state.sender);

    Player target = getServer().getPlayer(getPlayerName(ticket.placed_by));
    if (target != null) {
      Messages.sendMessage(target, "Command.Take.Notify", state.name); //$NON-NLS-1$
    }
  }

  /**
   * Teleport the player to the location from which a ticket was submitted
   *
   * @param state
   * @param args
   */
  private void cmdVisit(State state, String[] args) {
    if (state.isConsole) {
      Messages.sendMessage(state.player, "Error.Command.NoConsole"); //$NON-NLS-1$
      return;
    }
    if (!state.isAdmin) {
      Messages.sendMessage(state.player, "Error.Command.AdminOnly"); //$NON-NLS-1$
      return;
    }
    String id = args[0];
    if (!isTicket(id)) {
      Messages.sendMessage(state.player, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
      return;
    }
    Ticket ticket = Ticket.load(getTickets(), id);
    if (ticket == null) {
      Messages.sendMessage(state.sender, "Error.Ticket.InvalidNumber", id); //$NON-NLS-1$
      return;
    }

    if (state.player != null && !ticket.location.equals("none")) { //$NON-NLS-1$
      String[] vals = ticket.location.split(","); //$NON-NLS-1$
      World world = Bukkit.getWorld(vals[0]);
      double x = Double.parseDouble(vals[1]);
      double y = Double.parseDouble(vals[2]);
      double z = Double.parseDouble(vals[3]);
      state.player.teleport(new Location(world, x, y, z));
    }
  }

  /**
   * Add a ticket and increment the users ticket count
   *
   * @param id
   * @param user
   */
  protected void newTicket(String id, String user) {
    java.util.List<String> Tickets = getTickets().getStringList("Tickets"); //$NON-NLS-1$
    Tickets.add(id);
    getTickets().set("Tickets", Tickets); //$NON-NLS-1$
    getTickets().set("counts." + user, getTickets().getInt("counts." + user) + 1); //$NON-NLS-1$ //$NON-NLS-2$
  }
  final static long DAY_IN_MS = 1000 * 60 * 60 * 24;

  /**
   * Delete tickets older than the expiration period.
   */
  protected void expireTickets() {
    int days = getConfig().getInt("ResolvedTicketExpiration", 7); //$NON-NLS-1$
    if (days == 0) {
      return;
    }
    Date expiration = new Date(System.currentTimeMillis() - (days * TakeaNumber.DAY_IN_MS));
    java.util.List<String> Tickets = getTickets().getStringList("Tickets"); //$NON-NLS-1$
    int count = 0;
    log.log(Level.INFO, Messages.getString("Expire.Started")); //$NON-NLS-1$
    for (String id : Tickets) {
      try {
        Ticket ticket = Ticket.load(getTickets(), id);
        if (ticket.resolved_on != null) {
          Date resolved_on = date_format.parse(ticket.resolved_on);
          if (resolved_on.before(expiration)) {
            deleteTicket(id);
          }
        }
      } catch (ParseException e) {
        log.log(Level.WARNING, Messages.getString("Error.Date.Parse", e.getLocalizedMessage())); //$NON-NLS-1$
      }
    }
    log.log(Level.INFO, Messages.getString("Expire.Finished", count)); //$NON-NLS-1$
  }

  /**
   * Remove the ticket from the list
   *
   * @param ticket
   */
  protected void deleteTicket(String ticket) {
    // Decrement the users ticket count
    String user = "counts." + Ticket.load(getTickets(), ticket).placed_by; //$NON-NLS-1$
    int count = getTickets().getInt(user) - 1;
    getTickets().set(user, count < 1 ? null : count);

    // Remove the ticket entry
    java.util.List<String> Tickets = getTickets().getStringList("Tickets"); //$NON-NLS-1$
    Tickets.remove(ticket);
    getTickets().set("Tickets", Tickets); //$NON-NLS-1$
    getTickets().set(ticket, null);

    // Save changes
    saveTickets();
  }
  /**
   * Format for tickets
   */
  private static java.util.regex.Pattern ticket_format = java.util.regex.Pattern.compile("^\\d+$", java.util.regex.Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

  /**
   * Checks to see if a string represents a ticket id
   *
   * @param str string to check
   * @return true if the string matches the ticket format
   */
  protected boolean isTicket(String str) {
    return ticket_format.matcher(str).matches();
  }

  /**
   * Notify all online administrators
   *
   * @param message message to send
   */
  protected void notifyAdmins(String message) {
    if (!getConfig().getBoolean("NotifyAdminOnTicketClose")) { //$NON-NLS-1$
      return;
    }
    Player[] players = Bukkit.getOnlinePlayers();
    for (Player op : players) {
      if (op.hasPermission("tan.admin")) { //$NON-NLS-1$
        op.sendMessage(message);
      }
    }
  }
}
