package net.Indyuce.inventory.api.slot;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface VanillaSlotHandler {
	public void equip(Player player, ItemStack item);
	
	public boolean canEquip(ItemStack item);
	
//	public boolean isSlotEdited(Player player, int raw);
	
	public ItemStack retrieveItem(Player player);
}
