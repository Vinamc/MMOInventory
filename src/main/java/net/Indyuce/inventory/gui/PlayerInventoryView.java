package net.Indyuce.inventory.gui;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.InventoryData;
import net.Indyuce.inventory.api.NBTItem;
import net.Indyuce.inventory.api.event.ItemEquipEvent;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotRestriction;
import net.Indyuce.inventory.api.slot.SlotType;

public class PlayerInventoryView implements InventoryHolder {
	private final InventoryData data;
	private final Player player, target;

	private static final List<InventoryAction> supported = Arrays.asList(InventoryAction.PICKUP_ALL, InventoryAction.SWAP_WITH_CURSOR,
			InventoryAction.PLACE_ALL);

	public PlayerInventoryView(Player player) {
		this(player, player);
	}

	public PlayerInventoryView(Player player, Player target) {
		this.target = target;
		this.player = player;

		data = MMOInventory.plugin.getDataManager().getInventory(player);
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
		 * Supported clicks TODO add support for shift click with a feature that
		 * detects the slot he can put the item in
		 */
		if (!supported.contains(event.getAction())) {
			event.setCancelled(true);
			return;
		}

		/*
		 * Make sure the player is not trying to equip and item in a filler slot
		 * (bug fix)
		 */
		if (!isAir(event.getCurrentItem()) && event.getCurrentItem().isSimilar(MMOInventory.plugin.getSlotManager().getFiller().getItem())) {
			event.setCancelled(true);
			return;
		}

		// check if item can be equipped (apply slot restrictions)
		CustomSlot slot = MMOInventory.plugin.getSlotManager().get(event.getRawSlot());
		if (slot != null && !isAir(event.getCursor())) {

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

	// checks for both null and AIR material
	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
