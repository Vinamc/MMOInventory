package net.Indyuce.inventory.inventory;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.version.NBTItem;
import net.Indyuce.inventory.slot.CustomSlot;
import net.Indyuce.inventory.slot.SlotType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SimpleInventoryHandler extends InventoryHandler {

	/**
	 * Used when MMOInventory does not use the custom inventory and when the
	 * items must be placed in the vanilla player's inventory
	 */
	public SimpleInventoryHandler(Player player) {
		super(player);
	}

	@Override
	public Collection<ItemStack> getExtraItems() {
		return getItems(true);
	}

	@Override
	public Collection<ItemStack> getExtraItemsUnverified(Integer... excluded) {
		return getItems(false, excluded);
	}

	private Collection<ItemStack> getItems(boolean verify, Integer... excluded) {
		Set<ItemStack> set = new HashSet<>();

		// For each special slot
		for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getLoaded()) {

			// If its an accessory
			if (!isExcluded(slot, excluded) && slot.getType() == SlotType.ACCESSORY) {

				// Get that item
				ItemStack item = player.getInventory().getItem(slot.getIndex());

				// Is the item not a 'default' MMOInventory display thingy
				if (!MMOInventory.plugin.getVersionWrapper().getNBTItem(item).hasTag("MMOInventoryGuiItem") && !isAir(item)) {

					// Should it be verified? (Utilizing java operator short-circuiting!!)
					boolean verified = !verify || slot.checkSlotRestrictions(this, NBTItem.get(item));

					// Does it exist? Is it not a 'default' slot item? Does it meet slot restrictions?
					if (verified) {

						// Add it!
						set.add(item);
					}
				}
			}
		}

		return set;
	}

	private boolean isExcluded(CustomSlot slot, Integer... excludedIndexes) {
		for (int excluded : excludedIndexes)
			if (excluded == slot.getIndex())
				return true;
		return false;
	}

	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
