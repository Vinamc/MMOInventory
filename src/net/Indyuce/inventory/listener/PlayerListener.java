package net.Indyuce.inventory.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.manager.DataManager;

public class PlayerListener implements Listener {

	@EventHandler
	public void a(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		DataManager dataManager = MMOInventory.plugin.getDataManager();
		
		if (!dataManager.isInventoryLoaded(player))
			dataManager.loadInventory(player);
		else
			dataManager.getInventory(player).setPlayer(player);
	}
}
