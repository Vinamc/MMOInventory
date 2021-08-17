package net.Indyuce.inventory.compat.mmoitems;

import net.Indyuce.inventory.version.NBTItem;
import net.Indyuce.inventory.inventory.InventoryHandler;
import net.Indyuce.inventory.slot.CustomSlot;
import net.Indyuce.inventory.slot.SlotRestriction;
import net.Indyuce.mmoitems.api.player.PlayerData;

/**
 * Checks if the player has the required level/class/etc. to use the item.
 */
public class MMOItemsLevelRestriction extends SlotRestriction {


	@Override
	public boolean isVerified(InventoryHandler provider, CustomSlot slot, NBTItem item) {
		return PlayerData.get(provider.getPlayer()).getRPG().canUse(io.lumine.mythic.lib.api.item.NBTItem.get(item.getItem()), false);
	}
}
