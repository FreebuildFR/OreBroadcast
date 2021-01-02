package be.bendem.bukkit.orebroadcast.handlers;

import be.bendem.bukkit.orebroadcast.OreBroadcast;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BungeeCord Plugin Messaging Channel
 *
 * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">
 *     BungeeCord Plugin Messaging Channel
 *     </a>
 * @author G-Lauz
 */
public class PluginMessage implements PluginMessageListener {
    private final OreBroadcast plugin;

    public PluginMessage(OreBroadcast plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle the incoming message according to his parameters following this Packet format:
     * <ol type="1">
     *  <li>Subchannel ("OreBroadcast" his the only one handle here)</li>
     *  <li>Data length</li>
     *  <li>The actual data</li>
     * </ol>
     * Called when their is an incoming Plugin Channel message over the "BungeeCord" channel.
     *
     * @param channel The channel of the message (must be "BungeeCord")
     * @param player The player who send the Plugin channel message
     * @param message The message
     */
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord"))
            return;

        ByteArrayDataInput input = ByteStreams.newDataInput(message);
        String subchannel = input.readUTF();

        if (subchannel.equals("OreBroadcast")) {
            short len = input.readShort();
            byte[] msgbytes = new byte[len];
            input.readFully(msgbytes);

            DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
            try {
                String data = msgin.readUTF();

                Set<Player> recipients = getRecipients();
                broadcast(recipients, data);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Forward a Plugin Message to all server connect to BungeeCord with the following packet format
     * <ol type="1">
     *  <li>Subchannel ("OreBroadcast")</li>
     *  <li>Data length</li>
     *  <li>The actual data</li>
     * </ol>
     *
     * @param player The play who send the message
     * @param message The message to send
     */
    public void forwardMessage(Player player, String message) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        // Subchannel specification
        output.writeUTF("Forward");
        output.writeUTF("ALL");
        output.writeUTF("OreBroadcast");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);

        try {
            msgout.writeUTF(message);

            output.writeShort(msgbytes.toByteArray().length);
            output.write(msgbytes.toByteArray());

            player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcast a message to all recipient player and to the console
     *
     * @param recipients each recipient player
     * @param message the message to broadcast
     */
    public void broadcast(Set<Player> recipients, String message) {
        recipients.forEach(recipient -> recipient.sendMessage(message));

        Bukkit.getConsoleSender().sendMessage(message);
    }

    /**
     * Get a Set of each online player
     *
     * @return a Set of online player
     */
    public Set<Player> getRecipients() {
        return plugin.getServer().getOnlinePlayers().stream()
                .filter(onlinePlayer -> onlinePlayer.hasPermission("ob.receive"))
                .collect(Collectors.toSet());
    }
}
