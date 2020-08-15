package net.Indyuce.inventory.listener;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.NBTItem;
import net.Indyuce.inventory.api.slot.CustomSlot;

public class NoCustomInventory implements Listener {

	@EventHandler
	public void giveItemsOnJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getCustomSlots()) {
			ItemStack current = player.getInventory().getItem(slot.getIndex());
			if (slot.checkSlotRestrictions(MMOInventory.plugin.getDataManager().getInventory(player), current))
				continue;

			player.getInventory().setItem(slot.getIndex(), slot.getItem());

			/*
			 * Drops the item that was previously in that slot only if it was
			 * not a special MMOInv gui item
			 */
			if (current != null && current.getType() != Material.AIR && !NBTItem.get(current).hasTag("MMOInventoryGuiItem"))
				for (ItemStack drop : player.getInventory().addItem(current).values())
					player.getWorld().dropItem(player.getLocation(), drop);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void placeBackSlotItems(InventoryClickEvent event) {

		Player player = (Player) event.getWhoClicked();

		/*
		 * If the item picked up is a special slot item, delete it
		 */
		if (MMOInventory.plugin.getVersionWrapper().getNBTItem(event.getCurrentItem()).hasTag("MMOInventoryGuiItem"))
			event.setCurrentItem(null);

		/*
		 * If there is no item in the slot item, place the slot item back
		 */
		Bukkit.getScheduler().runTask(MMOInventory.plugin, () -> {
			for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getCustomSlots())
				if (isAir(player.getInventory().getItem(slot.getIndex()))) {
					player.getInventory().setItem(slot.getIndex(), slot.getItem());
					player.updateInventory();
				}
		});
	}

	@EventHandler
	public void removeGuiDrops(PlayerDeathEvent event) {
		if (event.getKeepInventory())
			return;

		Iterator<ItemStack> iterator = event.getDrops().iterator();
		while (iterator.hasNext()) {
			ItemStack next = iterator.next();
			if (NBTItem.get(next).hasTag("MMOInventoryGuiItem"))
				iterator.remove();
		}
	}

	@EventHandler
	public void giveItemsOnRespawn(PlayerRespawnEvent event) {
		giveItemsOnJoin(new PlayerJoinEvent(event.getPlayer(), "You found a secret dev easter egg"));
	}

	// checks for both null and AIR material
	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
