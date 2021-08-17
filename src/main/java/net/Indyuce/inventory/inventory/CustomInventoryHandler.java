package net.Indyuce.inventory.inventory;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.slot.CustomSlot;
import net.Indyuce.inventory.slot.SlotType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomInventoryHandler extends InventoryHandler {

	/**
	 * Items are stored inside a map with their corresponding slot because it is
	 * much easier to access every item at the same time. It also makes items
	 * accessible and EDITABLE by any other plugins.
	 */
	private final Map<Integer, ItemStack> items = new HashMap<>();

	/**
	 * Used when MMOInventory utilizes the custom inventory GUI
	 */
	public CustomInventoryHandler(Player player) {
		super(player);

		MMOInventory.plugin.getDataManager().load(this);
	}

	public void setItem(CustomSlot slot, ItemStack item) {
		if (slot.getType() == SlotType.ACCESSORY) {

			/*
			 * Map containing no NULL values makes it much easier for external
			 * plugins to manipulate the mapped itemStacks
			 */
			if (isAir(item))
				items.remove(slot.getIndex());

				/*
				 * Equip item i.e add a clone to the map. The item is cloned so that
				 * further modifications do not impact the instance stored in the
				 * custom inventory
				 */
			else
				items.put(slot.getIndex(), item.clone());

			// Equip vanilla items
		} else
			slot.getType().getVanillaSlotHandler().equip(player, item);
	}

	public Map<Integer, ItemStack> getMapped() {
		return items;
	}

	/**
	 * For custom slots, this checks for a key in the player's item map.
	 * For vanilla slots, this reaches in the player's inventory and see
	 * if it has equipped an item in the corresponding slot.
	 *
	 * @param slot Custom slot to check
	 * @return The item equipped by the player
	 */
	public ItemStack getItem(CustomSlot slot) {
		return slot.getType().isCustom() ? getItem(slot.getIndex()) : slot.getType().getVanillaSlotHandler().retrieveItem(player);
	}

	public ItemStack getItem(int slot) {
		return items.get(slot);
	}

	/**
	 * For custom slots, this checks for a key in the player's item map.
	 * For vanilla slots, this reaches in the player's inventory and see
	 * if it has equipped an item in the corresponding slot.
	 *
	 * @param slot Custom slot to check
	 * @return If the player has an item in a specific slot
	 */
	public boolean hasItem(CustomSlot slot) {
		return slot.getType().isCustom() ? items.containsKey(slot.getIndex()) : !isAir(slot.getType().getVanillaSlotHandler().retrieveItem(player));
	}

	@Override
	public Collection<ItemStack> getExtraItems() {
		return items.values();
	}

	@Override
	public Collection<ItemStack> getExtraItemsUnverified(Integer... excluded) {

		// Don't return the excluded values tho
		Collection<ItemStack> ret = items.values();

		// Remove excluded
		for (Integer ex : excluded) {

			// Get
			ItemStack value = items.get(ex);

			// Remove
			ret.remove(value);
		}

		return items.values();
	}

	public Set<Integer> getFilledSlotKeys() {
		return items.keySet();
	}

	public Set<CustomSlot> getFilledSlots() {
		return items.keySet().stream().map(id -> MMOInventory.plugin.getSlotManager().get(id)).collect(Collectors.toSet());
	}

	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
