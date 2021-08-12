package net.Indyuce.inventory.comp;

import net.Indyuce.inventory.api.LineConfig;
import net.Indyuce.inventory.api.NBTItem;
import net.Indyuce.inventory.api.inventory.InventoryHandler;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotRestriction;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class MMOItemsLevelRestriction extends SlotRestriction {

	/**
	 * Checks if the player has the required level/class/etc. to use the item.
	 */
	public MMOItemsLevelRestriction(LineConfig config) {
		super(config);
	}

	@Override
	public boolean isVerified(InventoryHandler provider, CustomSlot slot, NBTItem item) {
		return PlayerData.get(provider.getPlayer()).getRPG().canUse(io.lumine.mythic.lib.api.item.NBTItem.get(item.getItem()), false);
	}
}
