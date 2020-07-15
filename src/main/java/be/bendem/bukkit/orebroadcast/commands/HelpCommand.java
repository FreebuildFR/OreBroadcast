package be.bendem.bukkit.orebroadcast.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * @author bendem
 */
public class HelpCommand extends Command {

    private final CommandHandler handler;
    private final String pluginFullName;

    protected HelpCommand(CommandHandler handler, JavaPlugin plugin) {
        super("help", "Displays the commands you can use", null);
        this.handler = handler;
        this.pluginFullName = plugin.getDescription().getFullName();
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        StringBuilder builder = new StringBuilder()
            .append(ChatColor.GOLD)
            .append(this.pluginFullName)
            .append(ChatColor.RESET)
            .append("\nCommands: \n");
        for (Command command : handler.getCommands().values()) {
            if (command.hasPermission(sender)) {
                builder.append("- ").append(ChatColor.BLUE).append(command.getName()).append(ChatColor.RESET);
                if (command.getDescription() != null) {
                    builder.append(": ").append(command.getDescription());
                }
                builder.append('\n');
            }
        }
        sender.sendMessage(builder.deleteCharAt(builder.length() - 1).toString());
    }

}
