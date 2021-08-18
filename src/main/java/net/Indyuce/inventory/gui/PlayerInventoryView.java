package net.Indyuce.inventory.gui;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.event.ItemEquipEvent;
import net.Indyuce.inventory.inventory.CustomInventoryHandler;
import net.Indyuce.inventory.slot.CustomSlot;
import net.Indyuce.inventory.slot.SlotType;
import net.Indyuce.inventory.version.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class PlayerInventoryView implements InventoryHolder {
	private final CustomInventoryHandler data;
	private final Player player, target;

	private static final List<InventoryAction> supported = Arrays.asList(InventoryAction.PICKUP_ALL, InventoryAction.SWAP_WITH_CURSOR,
			InventoryAction.PLACE_ALL);

	public PlayerInventoryView(Player player) {
		this(player, player);
	}

	/**
	 * @param player Player opening the GUI and manipulating the items
	 * @param target The player owning the inventory
	 */
	public PlayerInventoryView(Player player, Player target) {
		this.target = target;
		this.player = player;

		data = (CustomInventoryHandler) MMOInventory.plugin.getDataManager().getInventory(target);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, MMOInventory.plugin.inventorySlots,
				target.equals(player) ? MMOInventory.plugin.getTranslation("inventory-name.self")
						: MMOInventory.plugin.getTranslation("inventory-name.other").replace("{name}", target.getName()));

		// Load custom items or vanilla items depending on slot type
		for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getLoaded()) {
			ItemStack item = slot.getType() == SlotType.ACCESSORY ? data.getItem(slot) : slot.getType().getVanillaSlotHandler().retrieveItem(target);
			inv.setItem(slot.getIndex(), isAir(item) ? slot.getItem() : item);
		}

		// Fill remaining inventory
		for (int j = 0; j < inv.getSize(); j++) {
			ItemStack item = inv.getItem(j);
			if (isAir(item))
				inv.setItem(j, MMOInventory.plugin.getSlotManager().getFiller().getItem());
		}

		return inv;
	}

	public void open() {
		player.openInventory(getInventory());
	}

	public void whenClicked(InventoryClickEvent event) {

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
		NBTItem picked = MMOInventory.plugin.getVersionWrapper().getNBTItem(event.getCurrentItem());
		if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			event.setCancelled(true);

			// Nothing to do
			if (isAir(event.getCurrentItem()) || picked.hasTag("MMOInventoryGuiItem"))
				return;

			if (event.getClickedInventory().equals(event.getView().getBottomInventory())) {

				// Find the best slot available
				CustomSlot best = findBestSlot(picked);
				if (best == null)
					return;

				ItemEquipEvent called = new ItemEquipEvent(target, event.getCurrentItem(), best, ItemEquipEvent.EquipAction.SHIFT_CLICK_EQUIP);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return;

				data.setItem(best, picked.getItem());
				event.getInventory().setItem(best.getIndex(), picked.getItem());
				event.setCurrentItem(null);

				// For all active watchers
				forEachWatcher(view -> view.getTopInventory().setItem(best.getIndex(), picked.getItem()));

			} else {

				// Get the clicked slot
				CustomSlot slot = MMOInventory.plugin.getSlotManager().get(event.getRawSlot());
				if (slot == null)
					return;

				// Find a place where to put the item
				int empty = player.getInventory().firstEmpty();
				if (empty == -1)
					return;

				ItemEquipEvent called = new ItemEquipEvent(target, event.getCurrentItem(), null, ItemEquipEvent.EquipAction.SHIFT_CLICK_UNEQUIP);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return;

				data.setItem(slot, null);
				player.getInventory().setItem(empty, event.getCurrentItem());
				event.setCurrentItem(slot.getItem());

				// For all active watchers
				forEachWatcher(view -> view.getTopInventory().setItem(slot.getIndex(), slot.getItem()));
			}

			return;
		}

		// Only a few types of clicks are supported
		if (!supported.contains(event.getAction())) {
			event.setCancelled(true);
			return;
		}

		// Prevent any interaction with filler slots
		if (picked.getString("MMOInventoryGuiItem").equals("FILL")) {
			event.setCancelled(true);
			return;
		}

		// Player tries to pickup a slot item, without equipping any
		if (picked.hasTag("MMOInventoryGuiItem") && isAir(event.getCursor())) {
			event.setCancelled(true);
			return;
		}

		// Check for clicked slot
		CustomSlot slot = MMOInventory.plugin.getSlotManager().get(event.getRawSlot());
		if (slot == null)
			return;

		// Check if item can be equipped (apply slot restrictions)
		NBTItem cursor = NBTItem.get(event.getCursor());
		if (!isAir(event.getCursor())) {

			// Prevents equipping stacked items
			if (MMOInventory.plugin.getConfig().getBoolean("disable-equiping-stacked-items", true) && event.getCursor().getAmount() > 1) {
				event.setCancelled(true);
				return;
			}

			// Check for vanilla AND custom slot restrictions
			if (!slot.canHost(data, cursor)) {
				event.setCancelled(true);
				return;
			}
		}

		/*
		 * May be called with a null item as parameter if the player is
		 * unequipping an item
		 */
		ItemEquipEvent.EquipAction action = isAir(event.getCursor()) ? ItemEquipEvent.EquipAction.UNEQUIP : cursor.hasTag("MMOInventoryGuiItem") ? ItemEquipEvent.EquipAction.EQUIP : ItemEquipEvent.EquipAction.SWAP_ITEMS;
		ItemEquipEvent equipEvent = new ItemEquipEvent(target, event.getCursor(), slot, action);
		Bukkit.getPluginManager().callEvent(equipEvent);
		if (equipEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		data.setItem(slot, event.getCursor());

		// For all active watchers
		ItemStack newItem = isAir(event.getCursor()) ? slot.getItem() : event.getCursor();
		forEachWatcher(view -> view.getTopInventory().setItem(slot.getIndex(), newItem));

		/*
		 * If the player has picked up an inventory slot item, remove it
		 * instantly after checking the equip event was not canceled (bug
		 * fix)
		 */
		if (picked.hasTag("MMOInventoryGuiItem"))
			event.setCurrentItem(null);

		/*
		 * If the player is taking away an item without swapping it, place
		 * the inventory slot item back in the corresponding slot.
		 *
		 * This must be done using a delayed task otherwise this will replace
		 * the event's current item
		 */
		if (isAir(event.getCursor()))
			Bukkit.getScheduler().runTask(MMOInventory.plugin, () -> event.getInventory().setItem(slot.getIndex(), slot.getItem()));

		// Finally update the player's inventory
		MMOInventory.plugin.updateInventory(player);
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

	/**
	 * This is used when an admin edits the inventory of another
	 * player, while the target player COULD have his inventory opened
	 * as well.
	 * <p>
	 * This method performs
	 */
	public void forEachWatcher(Consumer<InventoryView> consumer) {
		for (Player online : Bukkit.getOnlinePlayers())
			if (online.getOpenInventory() != null && online.getOpenInventory().getTopInventory().getHolder() instanceof PlayerInventoryView) {
				PlayerInventoryView customGui = (PlayerInventoryView) online.getOpenInventory().getTopInventory().getHolder();
				if (!equals(customGui) && customGui.target.equals(target))
					consumer.accept(online.getOpenInventory());
			}
	}

	/**
	 * @return Checks for both null and AIR material. Really
	 *         handy for events to check if something is happening or not
	 */
	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
