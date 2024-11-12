package xyz.emirdev.emirnametags.handlers;

import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.simpleyaml.configuration.file.YamlFile;
import xyz.emirdev.emirnametags.EmirNametags;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConfigHandler {
    private static final File DATA_FILE = new File(EmirNametags.get().getDataFolder(), "config.yml");

    private YamlFile yamlFile;

    public ConfigHandler() {
        loadFile();

        this.yamlFile.setComment("nametag", "The configuration for nametags.");
        this.yamlFile.setComment("nametag.interval", "The amount of ticks to wait between nametag updates.");
        this.yamlFile.addDefault("nametag.interval", 1);
        this.yamlFile.setComment("nametag.text", """
                The text of nametag.
                Supports PlaceholderAPI, MiniMessage and Skript.
                Placeholders must be in format: <placeholder:player_name>, <papi:player_name> or <p:player_name>
                Skript variables can be accessed with <skript:variable> or <sk:variable>. Supports %uuid%, example: <skript:level::%uuid%>""");
        this.yamlFile.addDefault("nametag.text", List.of("<p:displayname>"));
        this.yamlFile.setComment("nametag.billboard", """
                The billboard of nametag.
                Valid values: FIXED, VERTICAL, HORIZONTAL, CENTER""");
        this.yamlFile.addDefault("nametag.billboard", "VERTICAL");
        this.yamlFile.setComment("nametag.bgcolor", "The background color of nametag as ARGB.");
        this.yamlFile.addDefault("nametag.bgcolor", "0, 0, 0, 0");
        this.yamlFile.setComment("nametag.viewrange", "The view range of nametag.");
        this.yamlFile.addDefault("nametag.viewrange", 10);
        this.yamlFile.setComment("nametag.offset-y", "The Y offset of nametag.");
        this.yamlFile.addDefault("nametag.offset-y", 0.15);
        this.yamlFile.setComment("nametag.seeself", "Whether the player can see their own nametag.");
        this.yamlFile.addDefault("nametag.seeself", false);

        saveFile();
    }

    public void loadFile() {
        this.yamlFile = new YamlFile(DATA_FILE);
        try {
            this.yamlFile.createOrLoadWithComments();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveFile() {
        try {
            this.yamlFile.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNametagInterval() {
        return this.yamlFile.getInt("nametag.interval");
    }

    public List<String> getNametagText() {
        return this.yamlFile.getStringList("nametag.text");
    }

    public Display.Billboard getNametagBillboard() {
        return Display.Billboard.valueOf(this.yamlFile.getString("nametag.billboard"));
    }

    public Color getNametagBackgroundColor() {
        List<Integer> values = Arrays.stream(this.yamlFile.getString("nametag.bgcolor").split(", "))
                .map(Integer::valueOf)
                .toList();

        return Color.fromARGB(values.get(0), values.get(1), values.get(2), values.get(3));
    }

    public float getNametagViewRange() {
        return (float) this.yamlFile.getDouble("nametag.viewrange");
    }

    public float getNametagYOffset() {
        return (float) this.yamlFile.getDouble("nametag.offset-y");
    }

    public boolean canSeeSelfNametag() {
        return this.yamlFile.getBoolean("nametag.seeself");
    }
}