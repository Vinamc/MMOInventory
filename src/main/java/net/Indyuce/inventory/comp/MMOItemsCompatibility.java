package net.Indyuce.inventory.comp;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.event.ItemEquipEvent;
import net.Indyuce.inventory.gui.PlayerInventoryView;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.comp.inventory.PlayerInventory;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMOItemsCompatibility implements PlayerInventory, Listener {
	public MMOItemsCompatibility() {
		Bukkit.getPluginManager().registerEvents(this, MMOInventory.plugin);

		/*
		 * register with delay because MMOInventory does not always enable after
		 * MMOItems
		 */
		Bukkit.getScheduler().runTask(MMOInventory.plugin, () -> MMOItems.plugin.setPlayerInventory(MMOInventory.plugin.getConfig().getBoolean("ornaments-support") ? new OrnamentSupport() : this));
	}

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

	@EventHandler
	public void a(InventoryCloseEvent event) {
		if (event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof PlayerInventoryView)
			PlayerData.get((OfflinePlayer) event.getPlayer()).updateInventory();
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void b(ItemEquipEvent event) {
		Bukkit.getScheduler().runTaskLater(MMOInventory.plugin, () -> PlayerData.get(event.getPlayer()).updateInventory(), 0);
	}

	public class OrnamentSupport implements PlayerInventory {

		@Override
		public List<EquippedItem> getInventory(Player player) {
			List<EquippedItem> list = new ArrayList<>();

			list.add(new EquippedItem(player.getInventory().getItemInMainHand(), EquipmentSlot.MAIN_HAND));
			list.add(new EquippedItem(player.getInventory().getItemInOffHand(), EquipmentSlot.OFF_HAND));

			for (ItemStack armor : player.getInventory().getArmorContents())
				list.add(new EquippedItem(armor, EquipmentSlot.ARMOR));

			MMOInventory.plugin.getDataManager().getInventory(player).getExtraItems().forEach(item -> list.add(new EquippedItem(item, EquipmentSlot.ACCESSORY)));

			for (ItemStack item : player.getInventory().getContents()) {
				NBTItem nbtItem;
				if (item != null && (nbtItem = MMOLib.plugin.getNMS().getNBTItem(item)).hasType() && nbtItem.getType().getEquipmentType() == EquipmentSlot.ANY)
					list.add(new EquippedItem(nbtItem, EquipmentSlot.ANY));
			}

			return list;
		}
	}
}
