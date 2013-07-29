package me.olddragon.takeanumber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messages {

    private static YamlConfiguration messages;

    private Messages() {
    }

    public static void load(File file) {
        if (file.exists() && file.canRead()) {
            messages = YamlConfiguration.loadConfiguration(file);
        }
    }

    public static void setDefaults(InputStream is) {
        if (messages != null && is != null) {
            messages.setDefaults(YamlConfiguration.loadConfiguration(is));
        }
    }

    public static void save(File file) throws IOException {
        messages.save(file);
    }

    public static String getString(String key) {
        String msg = ChatColor.translateAlternateColorCodes('&', messages.getString(key));
        return msg != null ? msg : '!' + key + '!';
    }

    public static String getString(String key, Object... args) {
        return String.format(messages.getString(key), args);
    }

    public static void sendMessage(CommandSender sender, String key) {
        sender.sendMessage(getString(key));
    }

    public static void sendMessage(CommandSender sender, String key, Object... args) {
        sender.sendMessage(getString(key, args));
    }
}
