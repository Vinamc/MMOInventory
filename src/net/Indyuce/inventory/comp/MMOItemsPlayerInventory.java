package net.Indyuce.inventory.comp;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.Indyuce.mmoitems.comp.inventory.PlayerInventory;

public class MMOItemsPlayerInventory implements PlayerInventory {

	@Override
	public List<EquippedItem> getInventory(Player player) {
		List<EquippedItem> list = new ArrayList<>();

		list.add(new EquippedItem(player.getInventory().getItemInMainHand(), EquipmentSlot.MAIN_HAND));
		list.add(new EquippedItem(player.getInventory().getItemInOffHand(), EquipmentSlot.OFF_HAND));

		for (ItemStack armor : player.getInventory().getArmorContents())
			list.add(new EquippedItem(armor, EquipmentSlot.ARMOR));

		MMOInventory.plugin.getDataManager().getInventory(player).getExtraItems().forEach(item -> list.add(new EquippedItem(item, EquipmentSlot.ACCESSORY)));

		return list;
	}
}
