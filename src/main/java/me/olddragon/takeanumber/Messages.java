package me.olddragon.takeanumber;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
  
  public static String getFormatted(String key, Object... args) {
    try {
      return String.format(RESOURCE_BUNDLE.getString(key), args);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
}
