package xyz.emirdev.emirnametags;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Utils {
    public static Component getPrefix() {
        return format("<gradient:#00eeaa:#00aaaa><bold>EmirNametags<reset> <dark_gray>» ");
    }

    public static Component serialize(String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }

    public static Component format(String string, Object... args) {
        return MiniMessage.miniMessage().deserialize(String.format(string, args));
    }

    public static void sendMessage(CommandSender sender, String string, Object... args) {
        sender.sendMessage(getPrefix().append(format(string, args)));
    }

    public static void sendError(CommandSender sender, String string, Object... args) {
        sender.sendMessage(getPrefix().append(format("<#ee4444>" + string, args)));
    }

    public static void broadcast(String string, Object... args) {
        Bukkit.broadcast(getPrefix().append(format(string, args)));
    }

    public static String convertComponentToLegacyString(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}