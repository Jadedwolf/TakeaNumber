package me.olddragon.takeanumber;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.bukkit.command.CommandSender;

public class Util {
  private static SimpleDateFormat date_format = new SimpleDateFormat();
  public static String getCurrentDate () { return date_format.format (Calendar.getInstance().getTime()); }
  public static void setDateFormat(String format) { Util.date_format = new SimpleDateFormat(format); }
  public static Date parseDate(String date) { try { return Util.date_format.parse(date); } catch (ParseException e) { return new Date(0); } }
  
  /**
   * Send a formatted message
   * 
   * Will process ampersand color codes
   * 
   * @param sender who to send the message to
   * @param format the message to send
   */
  public static boolean sendMessage(CommandSender sender, String format) { return sendMessage(sender, format, new Object[] {}); }
  
  /**
   * Send a formatted message
   * 
   * Will process ampersand color codes
   * 
   * @param sender who to send the message to
   * @param format the message to send
   * @param value will replace a place holder
   */
  public static boolean sendMessage(CommandSender sender, String format, Object value) { return sendMessage(sender, format, new Object[] { value }); }
  
  /**
   * Send a formatted message
   * 
   * Will process ampersand color codes
   * 
   * @param sender who to send the message to
   * @param format the message to send
   * @param values will replace place holders
   */
  public static boolean sendMessage(CommandSender sender, String format, Object[] values) {
    if (sender == null) { return false; }
    if (format == null || format == "") { return false; }
    if (values == null) { values = new Object[] {}; }
    format = format.replaceAll("&([0-9a-fA-F])", "\u00A7$1");
    sender.sendMessage(String.format(format, values));
    return true;
  }
  
  /**
   * Return a Collection of elements that meet a criteria
   * @param target Collection to pull elements from
   * @param predicate Criteria to meet
   * @return A Collection of just the elements that meet the criteria
   */
  public static <T> Collection<T> filter(Collection<T> target, Predicate<T> predicate) {
    Collection<T> result = new ArrayList<T>();
    for (T element: target) { if (predicate.apply(element)) { result.add(element); } }
    return result;
  }
}
