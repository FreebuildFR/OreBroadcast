package be.bendem.bukkit.orebroadcast.handlers;

import be.bendem.bukkit.orebroadcast.OreBroadcast;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class PluginMessage implements PluginMessageListener {
    private OreBroadcast plugin;

    public PluginMessage(OreBroadcast plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if(!channel.equals("BungeeCord"))
            return;

        ByteArrayDataInput input = ByteStreams.newDataInput(message);
        String subchannel = input.readUTF();

        if(subchannel.equals("OreBroadcast")) {
            short len = input.readShort();
            byte[] msgbytes = new byte[len];
            input.readFully(msgbytes);

            DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
            try {
                String data = msgin.readUTF();

                Set<Player> recipients = getRecipients();
                broadcast(recipients, data);

            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

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

    public void broadcast(Set<Player> recipients, String message) {
        for (Player recipient : recipients) {
            recipient.sendMessage(message);
        }

        Bukkit.getConsoleSender().sendMessage(message);
    }

    public Set<Player> getRecipients() {
        Set<Player> recipients = new HashSet<>();
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("ob.receive")) {
                recipients.add(onlinePlayer);
            }
        }
        return recipients;
    }
}
