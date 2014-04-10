package me.olddragon.takeanumber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Handles displaying and sending messages stored in the YAML.
 */
public class Messages {

    private static YamlConfiguration yaml;

    private Messages() {
    }

    /**
     * Loads messages from a YAML file.
     *
     * @param file file that contains the messages
     */
    public static void load(File file) {
        if (file.exists() && file.canRead()) {
            yaml = YamlConfiguration.loadConfiguration(file);
        }
    }

    /**
     * Load the default messages if some or all are not customized.
     *
     * @param is input stream to load messages from.
     */
    public static void setDefaults(InputStream is) {
        if (yaml != null && is != null) {
            yaml.setDefaults(YamlConfiguration.loadConfiguration(is));
        }
    }

    /**
     * Save the messages to the plug-in resources folder to allow customization.
     *
     * @param file YAML file to save the messages too
     * @throws IOException thrown when unable to save a file.
     */
    public static void save(File file) throws IOException {
        if (yaml != null && file != null) {
            yaml.save(file);
        }
    }

    /**
     * Get a string from the messages by key.
     *
     * @param key message name/key/path
     * @return the message for the key or !key! if not found
     */
    public static String getString(String key) {
        String msg = yaml.getString(key);
        return msg != null ? ChatColor.translateAlternateColorCodes('&', msg) : '!' + key + '!';
    }

    /**
     * Get a string from the messages by key and fill in the format.
     *
     * @param key message name/key/path
     * @param args variables to fill in the format with
     * @return the message for the key with the arguments inserted or !key! if
     * not found
     */
    public static String getString(String key, Object... args) {
        return String.format(getString(key), args);
    }

    /**
     * Send a message to a player.
     *
     * @param sender reference to the player
     * @param key message name/key/path to send
     */
    public static void sendMessage(CommandSender sender, String key) {
        sender.sendMessage(getString(key));
    }

    /**
     * Send a message to a player after filling in the format.
     *
     * @param sender reference to the player
     * @param key message name/key/path to send
     * @param args variables to fill in the format with
     */
    public static void sendMessage(CommandSender sender, String key, Object... args) {
        sender.sendMessage(getString(key, args));
    }
}
