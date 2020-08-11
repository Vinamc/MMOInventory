package net.Indyuce.inventory.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.InventoryData;
import net.Indyuce.inventory.api.slot.CustomSlot;

public class DeathDrops implements Listener {

	@EventHandler
	public void dropItemsOnDeath(PlayerDeathEvent event) {
		if (!event.getKeepInventory()) {
			InventoryData data = MMOInventory.plugin.getDataManager().getInventory(event.getEntity());
			for (CustomSlot slot : data.getFilledSlots()) {
				ItemStack item = data.getItem(slot);
				data.setItem(slot, null);
				event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), item);
			}
		}
	}
}
