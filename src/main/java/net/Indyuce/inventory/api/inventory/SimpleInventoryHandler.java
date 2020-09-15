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
		Set<ItemStack> set = new HashSet<>();

		for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getLoaded())
			if (slot.getType() == SlotType.ACCESSORY) {
				ItemStack item = player.getInventory().getItem(slot.getIndex());
				if (!isAir(item) && slot.checkSlotRestrictions(this, item))
					set.add(item);
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
