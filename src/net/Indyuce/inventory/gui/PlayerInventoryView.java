package net.Indyuce.inventory.gui;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.InventoryData;
import net.Indyuce.inventory.api.event.ItemEquipEvent;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class PlayerInventoryView implements InventoryHolder {
	private final InventoryData data;
	private final Player player, target;
	private final PlayerData playerData;

	private static final List<InventoryAction> supported = Arrays.asList(InventoryAction.PICKUP_ALL, InventoryAction.SWAP_WITH_CURSOR, InventoryAction.PLACE_ALL);

	public PlayerInventoryView(Player player) {
		this(player, player);
	}

	public PlayerInventoryView(Player player, Player target) {
		this.target = target;
		this.player = player;
		this.playerData = PlayerData.get(player);

		data = MMOInventory.plugin.getDataManager().getInventory(player);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, MMOInventory.plugin.inventorySlots, target.equals(player) ? MMOInventory.plugin.getTranslation("inventory-name.self") : MMOInventory.plugin.getTranslation("inventory-name.other").replace("{name}", player.getName()));

		/*
		 * load custom items or vanilla items depending on slot type.
		 */
		for (CustomSlot slot : MMOInventory.plugin.getSlotManager().getLoaded())
			if (slot.isValid()) {
				ItemStack item = slot.getType().isCustom() ? data.getItem(slot) : slot.getType().getVanillaSlotHandler().retrieveItem(player);
				inv.setItem(slot.getIndex(), isAir(item) ? slot.getItem() : item);
			}

		/*
		 * fill remaining inventory
		 */
		for (int j = 0; j < inv.getSize(); j++) {
			ItemStack item = inv.getItem(j);
			if (isAir(item))
				inv.setItem(j, MMOInventory.plugin.getSlotManager().getFiller().getItem());
		}

		return inv;
	}

	public void open() {
		target.openInventory(getInventory());
	}

	public void whenClicked(InventoryClickEvent event) {

		if (!supported.contains(event.getAction())) {
			event.setCancelled(true);
			return;
		}

		/*
		 * cannot put items in the filler slots (BUG FIX)
		 */
		if (!isAir(event.getCurrentItem()) && event.getCurrentItem().isSimilar(MMOInventory.plugin.getSlotManager().getFiller().getItem())) {
			event.setCancelled(true);
			return;
		}

		CustomSlot slot = MMOInventory.plugin.getSlotManager().get(event.getRawSlot());
		if (slot != null && !isAir(event.getCursor()) && !slot.canEquip(event.getCursor())) {
			event.setCancelled(true);
			return;
		}

		/*
		 * remove the slot item if the player tried to pick it up.
		 */
		if (!isAir(event.getCurrentItem())) {
			NBTItem picked = MMOItems.plugin.getNMS().getNBTItem(event.getCurrentItem());
			if (picked.hasTag("inventoryItem")) {
				if (isAir(event.getCursor()) || picked.getString("inventoryItem").equals("FILL")) {
					event.setCancelled(true);
					return;
				}

				event.setCurrentItem(null);
			}
		}

		if (slot != null) {

			ItemEquipEvent equipEvent = new ItemEquipEvent(player, event.getCursor(), slot);
			Bukkit.getPluginManager().callEvent(equipEvent);
			if (equipEvent.isCancelled()) {
				event.setCancelled(true);
				return;
			}

			data.setItem(slot, event.getCursor());
			playerData.updateInventory();
			if (isAir(event.getCursor()))
				Bukkit.getScheduler().runTaskLater(MMOInventory.plugin, () -> event.getInventory().setItem(slot.getIndex(), slot.getItem()), 0);
		}
	}

	public void whenClosed(InventoryCloseEvent event) {
		playerData.updateInventory();
	}

	/*
	 * checks for both null and AIR material
	 */
	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
