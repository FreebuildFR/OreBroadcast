package be.bendem.bukkit.orebroadcast;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {
    private final OreBroadcast plugin;
    private final Set<SafeBlock> broadcastBlacklist = new HashSet<>();
    private final Set<Material> blocksToBroadcast = new HashSet<>();
    private final Set<String> worldWhitelist = new HashSet<>();
    private boolean worldWhitelistActive = false;

    protected Config(OreBroadcast plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    protected void loadConfig() {
        plugin.reloadConfig();
        // Create the list of materials to broadcast from the file
        List<String> configList = plugin.getConfig().getStringList("ores");
        blocksToBroadcast.clear();

        for (String item : configList) {
            Material material = this.getMaterial(item.toUpperCase());
            blocksToBroadcast.add(material);
        }

        // Load world whitelist
        worldWhitelist.clear();
        worldWhitelistActive = plugin.getConfig().getBoolean("active-per-worlds", true);
        if (worldWhitelistActive) {
            worldWhitelist.addAll(plugin.getConfig().getStringList("active-worlds"));
        }
    }

    protected Set<SafeBlock> getBroadcastBlacklist() {
        return broadcastBlacklist;
    }

    protected Set<Material> getBlocksToBroadcast() {
        return blocksToBroadcast;
    }

    protected Set<String> getWorldWhitelist() {
        return worldWhitelist;
    }

    protected boolean isWorldWhitelistActive() {
        return worldWhitelistActive;
    }

    private Material getMaterial(final String materialName) {
        final Material material = Material.getMaterial(materialName + "_ORE");
        return (material == null) ? Material.getMaterial(materialName) : material;
    }
}
