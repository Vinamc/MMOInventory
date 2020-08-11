package net.Indyuce.inventory.api.inventory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotRestriction;
import net.Indyuce.inventory.api.slot.SlotType;

public class SimpleInventoryHandler extends InventoryHandler {
	public SimpleInventoryHandler(Player player) {
		super(player);
	}

	@Override
	public Collection<ItemStack> getExtraItems() {
		Set<ItemStack> set = new HashSet<>();

		for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getLoaded())
			if (slot.getType() == SlotType.ACCESSORY) {
				ItemStack item = player.getInventory().getItem(slot.getIndex());
				if (!isAir(item) && canBeEquipped(slot, item))
					set.add(item);
			}

		return set;
	}

	@Override
	public void whenSaved() {
		// TODO Auto-generated method stub

	}

	private boolean canBeEquipped(CustomSlot slot, ItemStack item) {
		for (SlotRestriction restriction : slot.getRestrictions())
			if (!restriction.isVerified(this, slot, item))
				return false;
		return true;
	}

	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
