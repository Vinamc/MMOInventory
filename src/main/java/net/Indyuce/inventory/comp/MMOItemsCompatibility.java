package net.Indyuce.inventory.comp;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.event.ItemEquipEvent;
import net.Indyuce.inventory.gui.PlayerInventoryView;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MMOItemsCompatibility implements PlayerInventory, Listener {
	public MMOItemsCompatibility() {
		Bukkit.getPluginManager().registerEvents(this, MMOInventory.plugin);

		/*
		 * register with delay because MMOInventory does not always enable after
		 * MMOItems
		 */
		Bukkit.getScheduler().runTask(MMOInventory.plugin, () -> MMOItems.plugin.registerPlayerInventory(this));
	}

	@Override
	public List<EquippedItem> getInventory(Player player) {
		List<EquippedItem> list = new ArrayList<>();
		
		MMOInventory.plugin.getDataManager().getInventory(player).getExtraItems().forEach(item -> list.add(new EquippedItem(item, EquipmentSlot.ACCESSORY)));

		return list;
	}

	static String getItemName(ItemStack e) {
		if (e == null) { return "null"; }

		ItemMeta iMeta = e.getItemMeta();

		if (iMeta == null) {
			return e.getType().toString();
		}

		if (iMeta.hasDisplayName()) {

			return iMeta.getDisplayName();
		} else {

			return e.getType().toString();
		}
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
}
