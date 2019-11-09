package net.Indyuce.inventory.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.gui.PlayerInventoryView;

public class RPGInventoryCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(MMOInventory.plugin.getTranslation("player-command"));
			return true;
		}

		if (args.length < 1 && !(sender instanceof Player)) {
			sender.sendMessage(MMOInventory.plugin.getTranslation("specify-player"));
			return true;
		}

		if (args.length < 1) {
			new PlayerInventoryView((Player) sender).open();
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {
			MMOInventory.plugin.reload();
			sender.sendMessage(MMOInventory.plugin.getTranslation("reload").replace("{name}", MMOInventory.plugin.getName()).replace("{version}", MMOInventory.plugin.getDescription().getVersion()));
			return true;
		}

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			sender.sendMessage(MMOInventory.plugin.getTranslation("wrong-player").replace("{arg}", args[0]));
			return true;
		}

		new PlayerInventoryView(target, (Player) sender).open();
		return true;
	}
}
