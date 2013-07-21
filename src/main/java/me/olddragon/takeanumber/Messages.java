package me.olddragon.takeanumber;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.bukkit.command.CommandSender;

public class Messages {
  private static final String BUNDLE_NAME = "me.olddragon.takeanumber.messages"; //$NON-NLS-1$
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

  private Messages() {}

  public static String getString(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
  
  public static String getString(String key, Object... args) {
    try {
      return String.format(RESOURCE_BUNDLE.getString(key), args);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
  
  public static void sendMessage(CommandSender sender, String key) {
    sender.sendMessage(getString(key));
  }
  
  public static void sendMessage(CommandSender sender, String key, Object... args) {
    sender.sendMessage(getString(key, args));
  }
}
