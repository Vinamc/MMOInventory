package net.Indyuce.inventory.compat.mmoitems;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.event.ItemEquipEvent;
import net.Indyuce.inventory.compat.InventoryUpdater;
import net.Indyuce.inventory.gui.PlayerInventoryView;
import net.Indyuce.inventory.inventory.InventoryLookupMode;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import net.Indyuce.mmoitems.comp.inventory.PlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;

public class MMOItemsCompatibility implements PlayerInventory, Listener, InventoryUpdater {
	public MMOItemsCompatibility() {
		Bukkit.getPluginManager().registerEvents(this, MMOInventory.plugin);
		MMOInventory.plugin.registerInventoryUpdater(this);

		/*
		 * Register with delay because MMOInventory does
		 * not always enable after MMOItems
		 */
		Bukkit.getScheduler().runTask(MMOInventory.plugin, () -> MMOItems.plugin.registerPlayerInventory(this));
	}

	@Override
	public List<EquippedItem> getInventory(Player player) {
		List<EquippedItem> list = new ArrayList<>();
		
		MMOInventory.plugin.getDataManager().getInventory(player).getItems(InventoryLookupMode.NORMAL).forEach(item -> list.add(new EquippedItem(item.getItemStack(), EquipmentSlot.ACCESSORY)));

		return list;
	}

	@EventHandler
	public void updateWhenClosing(InventoryCloseEvent event) {
		if (event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof PlayerInventoryView)
			PlayerData.get((OfflinePlayer) event.getPlayer()).updateInventory();
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void updateWhenEquippingItem(ItemEquipEvent event) {
		Bukkit.getScheduler().runTaskLater(MMOInventory.plugin, () -> PlayerData.get(event.getPlayer()).updateInventory(), 0);
	}

	@Override
	public void updateInventory(Player player) {
		PlayerData.get(player).updateInventory();
	}
}
