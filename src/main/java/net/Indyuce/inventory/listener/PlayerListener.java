package net.Indyuce.inventory.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.manager.DataManager;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void loadInventoryData(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		DataManager dataManager = MMOInventory.plugin.getDataManager();

		/*
		 * Loads inventory data
		 */
		if (!dataManager.isInventoryLoaded(player))
			dataManager.loadInventory(player);

		/*
		 * Refreshes the player instance if inventory data has already been
		 * loaded
		 */
		else
			dataManager.getInventory(player).setPlayer(player);
	}
}
