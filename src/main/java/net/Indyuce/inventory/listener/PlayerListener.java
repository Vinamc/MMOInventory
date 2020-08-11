package net.Indyuce.inventory.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.Indyuce.inventory.MMOInventory;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void loadInventoryData(PlayerJoinEvent event) {
		MMOInventory.plugin.getDataManager().setupData(event.getPlayer());
	}
}
