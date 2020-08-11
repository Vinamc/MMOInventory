package net.Indyuce.inventory.listener;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.InventoryData;
import net.Indyuce.inventory.api.NBTItem;
import net.Indyuce.inventory.api.event.ItemEquipEvent;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotRestriction;
import net.Indyuce.inventory.api.slot.SlotType;

public class VanillaInventorySlots implements Listener {
	private static final List<InventoryAction> supported = Arrays.asList(InventoryAction.PICKUP_ALL, InventoryAction.SWAP_WITH_CURSOR,
			InventoryAction.PLACE_ALL);

	@EventHandler
	public void giveItemsOnJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getCustomSlots()) {
			ItemStack current = player.getInventory().getItem(slot.getIndex());
			player.getInventory().setItem(slot.getIndex(), slot.getItem());

			// drop the previous item if it was removed from the player's inv
			if (current != null)
				for (ItemStack drop : player.getInventory().addItem(current).values())
					player.getWorld().dropItem(player.getLocation(), drop);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void whenClicked(InventoryClickEvent event) {
		if (event.getInventory().getType() != InventoryType.CRAFTING)
			return;

		/*
		 * Supported clicks TODO add support for shift click with a feature that
		 * detects the slot he can put the item in
		 */
		CustomSlot slot = MMOInventory.plugin.getSlotManager().get(event.getRawSlot());
		if (slot == null)
			return;

		if (!supported.contains(event.getAction())) {
			event.setCancelled(true);
			return;
		}

		Player player = (Player) event.getWhoClicked();
		InventoryData data = MMOInventory.plugin.getDataManager().getInventory(player);

		// check if item can be equipped (apply slot restrictions)
		if (!isAir(event.getCursor())) {

			// vanilla slots requirements check
			if (slot.getType() != SlotType.ACCESSORY && !slot.getType().getVanillaSlotHandler().canEquip(event.getCursor())) {
				event.setCancelled(true);
				return;
			}

			// check for custom slot restrictions
			for (SlotRestriction restriction : slot.getRestrictions())
				if (!restriction.isVerified(data, slot, event.getCursor())) {
					event.setCancelled(true);
					return;
				}
		}

		/*
		 * When a player equips an item and swaps his cursor (item being
		 * equipped) with the current item ie the inventory slot item (eg
		 * Chestplate Slot). The inventory slot must be deleted
		 */
		NBTItem picked = MMOInventory.plugin.getVersionWrapper().getNBTItem(event.getCurrentItem());
		if (picked.hasTag("inventoryItem") && isAir(event.getCursor())) {
			event.setCancelled(true);
			return;
		}

		/*
		 * May be called with a null item as parameter if the player is
		 * unequipping an item
		 */
		ItemEquipEvent equipEvent = new ItemEquipEvent(player, event.getCursor(), slot);
		Bukkit.getPluginManager().callEvent(equipEvent);
		if (equipEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		if (picked.hasTag("inventoryItem"))
			event.setCurrentItem(null);

		data.setItem(slot, event.getCursor());
		if (isAir(event.getCursor()))
			Bukkit.getScheduler().runTask(MMOInventory.plugin, () -> player.getInventory().setItem(slot.getIndex(), slot.getItem()));
	}

	// checks for both null and AIR material
	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
