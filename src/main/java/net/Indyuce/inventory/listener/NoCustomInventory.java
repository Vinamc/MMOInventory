package net.Indyuce.inventory.listener;

import java.util.Iterator;

import net.Indyuce.inventory.api.event.ItemEquipEvent;
import net.Indyuce.inventory.api.inventory.InventoryHandler;
import net.Indyuce.inventory.api.slot.SlotType;
import net.Indyuce.mmoitems.api.player.PlayerData;
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

import static org.bukkit.Bukkit.getServer;

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
		if (MMOInventory.plugin.getVersionWrapper().getNBTItem(event.getCurrentItem()).hasTag("MMOInventoryGuiItem")) { event.setCurrentItem(null); }

		/*
		 * Check that the item can be placed in that slot. Cancel the event if it cant be.
		 */
		InventoryHandler iHandler = MMOInventory.plugin.getDataManager().getInventory(player);
		ItemStack iCursor = event.getCursor();
		if (!isAir(iCursor)) {

			// Get Slot
			CustomSlot iSlot = MMOInventory.plugin.getSlotManager().get(event.getRawSlot());

			// Existed?
			if (iSlot != null) {

				// Is it a custom accessory slot?
				boolean isAccessory = iSlot.getType() == SlotType.ACCESSORY;

				// Can the player equip thay item?
				boolean canEquip = true;
				if (iSlot.getType().getVanillaSlotHandler() != null) { canEquip = iSlot.getType().getVanillaSlotHandler().canEquip(event.getCursor()); }

				// Does the player bypass restrictions?
				boolean meetsRestrictions = iSlot.checkSlotRestrictions(iHandler, iCursor);

				// Cancel event if these go wrong
				if ((!isAccessory && !canEquip) || !meetsRestrictions) {

					// Due to reasons above, this item cannot be quipped.
					event.setCancelled(true);

				// Player may equip this item in thay slot
				} else {

					// Run Equip Event
					ItemEquipEvent equipEvent = new ItemEquipEvent(player, iCursor, iSlot);
					Bukkit.getPluginManager().callEvent(equipEvent);

					// Was it cancelled??
					if (equipEvent.isCancelled()) {

						// Cancel this event
						event.setCancelled(true);
					}
				}
			}
		}

		/*
		 * If there is no item in the slot item, place the slot item back
		 */
		Bukkit.getScheduler().runTask(MMOInventory.plugin, () -> {
			boolean needsUpdate = false;
			for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getCustomSlots()) {
				if (isAir(player.getInventory().getItem(slot.getIndex()))) {
					player.getInventory().setItem(slot.getIndex(), slot.getItem());
					player.updateInventory();
					needsUpdate = true;
				}
			}
			if (needsUpdate) { PlayerData.get(player).updateInventory();}
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
