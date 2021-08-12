package net.Indyuce.inventory.gui;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.NBTItem;
import net.Indyuce.inventory.api.event.ItemEquipEvent;
import net.Indyuce.inventory.api.inventory.CustomInventoryHandler;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class PlayerInventoryView implements InventoryHolder {
	private final CustomInventoryHandler data;
	private final Player player, target;

	private static final List<InventoryAction> supported = Arrays.asList(InventoryAction.PICKUP_ALL, InventoryAction.SWAP_WITH_CURSOR,
			InventoryAction.PLACE_ALL);

	public PlayerInventoryView(Player player) {
		this(player, player);
	}

	public PlayerInventoryView(Player player, Player target) {
		this.target = target;
		this.player = player;

		data = (CustomInventoryHandler) MMOInventory.plugin.getDataManager().getInventory(player);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, MMOInventory.plugin.inventorySlots,
				target.equals(player) ? MMOInventory.plugin.getTranslation("inventory-name.self")
						: MMOInventory.plugin.getTranslation("inventory-name.other").replace("{name}", player.getName()));

		/*
		 * Load custom items or vanilla items depending on slot type
		 */
		for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getLoaded())
			if (slot.isValid()) {
				ItemStack item = slot.getType() == SlotType.ACCESSORY ? data.getItem(slot)
						: slot.getType().getVanillaSlotHandler().retrieveItem(player);
				inv.setItem(slot.getIndex(), isAir(item) ? slot.getItem() : item);
			}

		/*
		 * Fill remaining inventory
		 */
		for (int j = 0; j < inv.getSize(); j++) {
			ItemStack item = inv.getItem(j);
			if (isAir(item))
				inv.setItem(j, MMOInventory.plugin.getSlotManager().getFiller().getItem());
		}

		return inv;
	}

	public void open() {
		target.openInventory(getInventory());
	}

	public void whenClicked(InventoryClickEvent event) {

		/*
		 * The player cannot edit this inventory if it is not theirs
		 */
		if (!target.equals(player)) {
			event.setCancelled(true);
			return;
		}

		/*
		 * Shift clicking finds the ONE slot that can host your item
		 * and automatically sends it there. There's a distinction to
		 * be made between clicking in the player inventory and clicking
		 * on an item which has already been equipped and which has to
		 * be sent back to the player's inventory.
		 *
		 * There's also a problem with item amounts? It's much simpler
		 * to just place all the stacked items as it's anyways quite
		 * rare to have accessories with max stacks size greater than 1
		 */
		if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			event.setCancelled(true);

			// Nothing to do
			if (isAir(event.getCurrentItem()))
				return;

			if (event.getClickedInventory().equals(event.getView().getBottomInventory())) {

				// Find the best slot available
				NBTItem picked = NBTItem.get(event.getCurrentItem());
				CustomSlot best = findBestSlot(picked);
				if (best == null)
					return;

				data.setItem(best, picked.getItem());
				event.getInventory().setItem(best.getIndex(), picked.getItem());
				event.setCurrentItem(null);

			} else {

				// Get the clicked slot
				CustomSlot slot = MMOInventory.plugin.getSlotManager().get(event.getRawSlot());
				if (slot == null)
					return;

				// Find a place where to put the item
				int empty = player.getInventory().firstEmpty();
				if (empty == -1)
					return;

				data.setItem(slot, null);
				player.getInventory().setItem(empty, event.getCurrentItem());
				event.setCurrentItem(slot.getItem());
			}

			return;
		}

		/*
		 * Only a few types of clicks are supported
		 */
		if (!supported.contains(event.getAction())) {
			event.setCancelled(true);
			return;
		}

		/*
		 * Make sure the player is not trying to
		 * equip and item in a filler slot (bug fix)
		 */
		NBTItem picked = MMOInventory.plugin.getVersionWrapper().getNBTItem(event.getCurrentItem());
		if (!isAir(event.getCurrentItem()) && picked.getString("MMOInventoryGuiItem").equals("FILL")) {
			event.setCancelled(true);
			return;
		}

		// Check if item can be equipped (apply slot restrictions)
		CustomSlot slot = MMOInventory.plugin.getSlotManager().get(event.getRawSlot());
		if (slot != null && !isAir(event.getCursor())) {

			// Prevents equipping stacked items
			if (MMOInventory.plugin.getConfig().getBoolean("disable-equiping-stacked-items", true) && event.getCursor().getAmount() > 1) {
				event.setCancelled(true);
				return;
			}

			// Check for vanilla AND custom slot restrictions
			if (!slot.canHost(data, NBTItem.get(event.getCursor()))) {
				event.setCancelled(true);
				return;
			}
		}

		/*
		 * When a player equips an item and swaps his cursor (item being
		 * equipped) with the current item ie the inventory slot item (eg
		 * Chestplate Slot). The inventory slot must be deleted
		 */
		if (picked.hasTag("MMOInventoryGuiItem") && (isAir(event.getCursor()) || picked.getString("MMOInventoryGuiItem").equals("FILL"))) {
			event.setCancelled(true);
			return;
		}

		if (slot != null) {

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

			data.setItem(slot, event.getCursor());

			/*
			 * If the player has picked up an inventory slot item, remove it
			 * instantly after checking the equip event was not canceled (bug
			 * fix)
			 */
			if (picked.hasTag("MMOInventoryGuiItem"))
				event.setCurrentItem(null);

			/*
			 * If the player is taking away an item without swapping it, place
			 * the inventory slot item back in the corresponding slot
			 */
			if (isAir(event.getCursor()))
				Bukkit.getScheduler().runTaskLater(MMOInventory.plugin, () -> event.getInventory().setItem(slot.getIndex(), slot.getItem()), 0);
		}
	}

	/**
	 * Used when shift clicking to find the best slot
	 * available for a specific item.
	 *
	 * @param item The item being shift clicked
	 * @return The best slot available for that item, or none if there isn't any.
	 *         Shift clicking should not do anything then
	 */
	private CustomSlot findBestSlot(NBTItem item) {

		for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getLoaded())
			if ((slot.getType().isCustom() || slot.getType().getVanillaSlotHandler().supportsShiftClick()) && !data.hasItem(slot) && slot.canHost(data, item))
				return slot;

		return null;
	}

	// checks for both null and AIR material
	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
