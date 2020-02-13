package net.Indyuce.inventory.comp;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.api.InventoryData;
import net.Indyuce.inventory.api.LineConfig;
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
	public boolean isVerified(Player player, InventoryData data, CustomSlot slot, ItemStack item) {
		return PlayerData.get(player).getRPG().canUse(NBTItem.get(item), false);
	}
}
