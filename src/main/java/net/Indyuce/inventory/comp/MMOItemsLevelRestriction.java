package net.Indyuce.inventory.comp;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.api.LineConfig;
import net.Indyuce.inventory.api.inventory.InventoryHandler;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotRestriction;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMOItemsLevelRestriction extends SlotRestriction {

	/*
	 * checks if the player has the required level/class/etc. to use the item.
	 */
	public MMOItemsLevelRestriction(LineConfig config) {
		super(config);
	}

	@Override
	public boolean isVerified(InventoryHandler provider, CustomSlot slot, ItemStack item) {
		return PlayerData.get(provider.getPlayer()).getRPG().canUse(NBTItem.get(item), false);
	}
}
