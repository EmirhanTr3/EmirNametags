package xyz.emirdev.emirnametags.commands;

import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import xyz.emirdev.emirnametags.EmirNametags;
import xyz.emirdev.emirnametags.Utils;

@Command("emirnametags")
@CommandPermission("emirnametags.command")
public class MainCommand {

    @Subcommand("reload")
    @CommandPermission("emirnametags.command.reload")
    public void reload(CommandSender sender) {
        EmirNametags.get().reloadPluginConfig();
        Utils.sendMessage(sender, "<green>Successfully reloaded EmirNametags.</green>");
    }
}
