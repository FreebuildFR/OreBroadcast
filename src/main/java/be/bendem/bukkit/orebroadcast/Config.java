package be.bendem.bukkit.orebroadcast;

import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class Config {

    private final OreBroadcast plugin;
    private final Set<SafeBlock> broadcastBlacklist = new HashSet<>();
    private final Set<Material> blocksToBroadcast = new HashSet<>();
    private final Set<String> worldWhitelist = new HashSet<>();
    private final Set<UUID> optOutPlayers = new HashSet<>();
    private final File playerFile;
    private boolean worldWhitelistActive = false;
    
    protected Config(OreBroadcast plugin) {
        this.plugin = plugin;
        playerFile = new File(plugin.getDataFolder(), "players.dat");
        plugin.saveDefaultConfig();
    }
    
    protected void loadConfig() {
        plugin.reloadConfig();
        // Create the list of materials to broadcast from the file
        List<String> configList = plugin.getConfig().getStringList("ores");
        blocksToBroadcast.clear();

        for (String item : configList) {
            Material material = Material.getMaterial(item.toUpperCase() + "_ORE");
            blocksToBroadcast.add(material);
        }

        // Load world whitelist
        worldWhitelist.clear();
        worldWhitelistActive = plugin.getConfig().getBoolean("active-per-worlds", true);
        if (worldWhitelistActive) {
            worldWhitelist.addAll(plugin.getConfig().getStringList("active-worlds"));
        }

        // Handling metrics changes
        boolean prev = metricsActive;
        metricsActive = plugin.getConfig().getBoolean("metrics", true);
        if (prev != metricsActive) {
            if (metricsActive) {
                startMetrics();
            } else {
                stopMetrics();
            }
        }

        // Updater thingy
        updater = new OreBroadcastUpdater(plugin, plugin.getJar());
        if (plugin.getConfig().getBoolean("updater.startup-check", true)) {
            updater.checkUpdate(null, false);
        }
        if (plugin.getConfig().getBoolean("updater.warn-ops", true)) {
            plugin.getServer().getPluginManager().registerEvents(new PlayerLoginListener(plugin), plugin);
        }

        // Load opt out players
        if (!playerFile.exists()) {
            return;
        }
        optOutPlayers.clear();
        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(playerFile))) {
            @SuppressWarnings("unchecked")
            Set<UUID> uuids = (Set<UUID>) stream.readObject();
            optOutPlayers.addAll(uuids);
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to read opt out players from file");
            e.printStackTrace(System.err);
        } catch (ClassCastException e) {
            plugin.getLogger().severe("Invalid players.dat file");
            e.printStackTrace(System.err);
        }
    }

    protected boolean isOptOut(UUID uuid) {
        return optOutPlayers.contains(uuid);
    }

    protected void optOutPlayer(UUID uuid) {
        optOutPlayers.add(uuid);
        saveOptOutPlayers();
    }

    protected void unOptOutPlayer(UUID uuid) {
        optOutPlayers.remove(uuid);
        saveOptOutPlayers();
    }

    private void saveOptOutPlayers() {
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(playerFile))) {
            stream.writeObject(optOutPlayers);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write opt out players to file");
            e.printStackTrace(System.err);
        }
    }

    protected void startMetrics() {
        if (metrics == null) {
            try {
                metrics = new Metrics(plugin);
            } catch (IOException e) {
                plugin.getLogger().warning("Couldn't activate metrics :(");
                return;
            }
        }
        metrics.start();
    }

    protected void stopMetrics() {
        if (metrics == null) {
            return;
        }
        // This is temporary while waiting for https://github.com/Hidendra/Plugin-Metrics/pull/43
        try {
            Field taskField = metrics.getClass().getDeclaredField("task");
            taskField.setAccessible(true);
            BukkitTask task = (BukkitTask) taskField.get(metrics);
            if (task != null) {
                task.cancel();
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().log(Level.WARNING, "Error while stopping metrics, please report this to the plugin author", e);
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
}
