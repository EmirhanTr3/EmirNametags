package xyz.emirdev.emirnametags;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import xyz.emirdev.emirnametags.commands.MainCommand;
import xyz.emirdev.emirnametags.handlers.ConfigHandler;
import xyz.emirdev.emirnametags.nametag.NametagManager;
import xyz.emirdev.emirnametags.nametag.TextParser;

public final class EmirNametags extends JavaPlugin {
    private static EmirNametags instance;
    private ConfigHandler config;
    private NametagManager nametagManager;
    private boolean papiEnabled;
    private boolean luckPermsEnabled;
    private LuckPerms luckPerms;
    private boolean skriptEnabled;

    public static EmirNametags get() {
        return instance;
    }

    public ConfigHandler getPluginConfig() {
        return config;
    }

    public void reloadPluginConfig() {
        TextParser.clearPlaceholderCache();
        config.loadFile();
        nametagManager.reloadNametags();
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    public boolean isLuckPermsEnabled() {
        return luckPermsEnabled;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public boolean isSkriptEnabled() {
        return skriptEnabled;
    }

    @Override
    public void onEnable() {
        instance = this;
        config = new ConfigHandler();
        nametagManager = new NametagManager();
        papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        skriptEnabled = Bukkit.getPluginManager().isPluginEnabled("Skript");

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPermsEnabled = true;
            luckPerms = provider.getProvider();
        } else {
            luckPermsEnabled = false;
        }

        Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this).build();
        lamp.register(new MainCommand());

        getServer().getPluginManager().registerEvents(nametagManager, this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}
