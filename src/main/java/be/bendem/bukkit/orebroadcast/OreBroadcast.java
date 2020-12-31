package be.bendem.bukkit.orebroadcast;

import be.bendem.bukkit.orebroadcast.commands.Command;
import be.bendem.bukkit.orebroadcast.commands.CommandHandler;
import be.bendem.bukkit.orebroadcast.handlers.BlockBreakListener;
import be.bendem.bukkit.orebroadcast.handlers.PluginMessage;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;

/**
 * OreBroadcast for Bukkit
 *
 * @author bendem
 */
public class OreBroadcast extends JavaPlugin {
    private Config config;

    public void onEnable() {
        config = new Config(this);
        config.loadConfig();

        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);

        CommandHandler commandHandler = new CommandHandler(this, "ob");
        commandHandler.register(new Command("reload", "Reloads OreBroadcast config", "ob.commands.reload") {
            @Override
            public void execute(CommandSender sender, List<String> args) {
                config.loadConfig();
                sender.sendMessage("Config reloaded...");
            }
        });

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessage(this));
    }

    /**
     * Blacklists a block. Blocks blacklisted won't get broadcasted when broken.
     *
     * @param block the block to blacklist
     */
    public void blackList(Block block) {
        config.getBroadcastBlacklist().add(new SafeBlock(block));
    }

    /**
     * Blacklists multiple blocks. Blocks blacklisted won't get broadcasted when
     * broken.
     *
     * @param blocks the blocks to blacklist
     */
    public void blackList(Collection<Block> blocks) {
        for (Block block : blocks) {
            blackList(block);
        }
    }

    /**
     * Unblacklist a block.
     *
     * @param block the block to unblacklist
     */
    public void unBlackList(Block block) {
        config.getBroadcastBlacklist().remove(new SafeBlock(block));
    }

    /**
     * Unblacklist multiple blocks.
     *
     * @param blocks the blocks to unblacklist
     */
    public void unBlackList(Collection<Block> blocks) {
        for (Block block : blocks) {
            unBlackList(block);
        }
    }

    /**
     * Checks wether a block is blacklisted or not.
     *
     * @param block the block to check
     * @return true if the block is blacklisted
     */
    public boolean isBlackListed(Block block) {
        return config.getBroadcastBlacklist().contains(new SafeBlock(block));
    }

    /**
     * Checks wether a material should be broadcasted when broken
     *
     * @param material the material to check
     * @return true if breaking a block of that material will trigger a
     *         broadcast
     */
    public boolean isWhitelisted(Material material) {
        return config.getBlocksToBroadcast().contains(material);
    }

    /**
     * Check if OreBroadcast is active in a world
     *
     * @param world the world to check if OreBroadcast is active in
     * @return true if OreBroadcast is active in the world
     */
    public boolean isWorldWhitelisted(World world) {
        return isWorldWhitelisted(world.getName());
    }

    /**
     * Check if OreBroadcast is active in a world
     *
     * @param world the name of the world
     * @return true if OreBroadcast is active in the world
     */
    public boolean isWorldWhitelisted(String world) {
        return !config.isWorldWhitelistActive() || config.getWorldWhitelist().contains(world);
    }
}
