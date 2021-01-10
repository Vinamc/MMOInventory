package net.Indyuce.inventory.api.inventory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotType;

import static org.bukkit.Bukkit.getServer;

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

	Collection<ItemStack> getItems(boolean verify, Integer... excluded) {
		Set<ItemStack> set = new HashSet<>();

		// For each special slot
		for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getLoaded()) {

			// Is it excluded?
			boolean isExcluded = false;
			for (Integer ex : excluded) {
				if (ex == slot.getIndex()) {
					isExcluded = true;
					break;
				}
			}

			// If its an accessory
			if (!isExcluded && slot.getType() == SlotType.ACCESSORY) {

				// Get that item
				ItemStack item = player.getInventory().getItem(slot.getIndex());

				// Is the item not a 'default' MMOInventory display thingy
				if (!MMOInventory.plugin.getVersionWrapper().getNBTItem(item).hasTag("MMOInventoryGuiItem") && !isAir(item)) {

					// Should it be verified?
					boolean verified = !verify;
					if (!verified) {
						verified = slot.checkSlotRestrictions(this, item);
					}

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

	@Override
	public void whenSaved() {}
	@Override
	public void whenSavedSQL() {}

	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
