package net.Indyuce.inventory.api.inventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.ConfigFile;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotType;

public class CustomInventoryHandler extends InventoryHandler {

	/*
	 * items are stored inside a map with their corresponding slot because it is
	 * much easier to access every item at the same time. it also makes items
	 * accessible and EDITABLE by any other plugins.
	 */
	public final Map<Integer, ItemStack> items = new HashMap<>();

	public CustomInventoryHandler(Player player) {
		super(player);

		FileConfiguration config = new ConfigFile("/userdata", player.getUniqueId().toString()).getConfig();

		if (config.contains("inventory"))
			for (String key : config.getConfigurationSection("inventory").getKeys(false))
				try {
					int index = Integer.parseInt(key);
					items.put(index, config.getItemStack("inventory." + index));
				} catch (IllegalArgumentException exception) {
					MMOInventory.plugin.getLogger().log(Level.SEVERE,
							"Could not read inventory item indexed " + key + " of " + player.getName() + ": " + exception.getMessage());
				}
	}

	public void setItem(CustomSlot slot, ItemStack item) {
		if (slot.getType() == SlotType.ACCESSORY) {

			/*
			 * Map containing no NULL values makes it much easier for external
			 * plugins to manipulate the mapped itemStacks
			 */
			if (isAir(item))
				items.remove(slot.getIndex());

			/*
			 * Equip item i.e add a clone to the map. The item is cloned so that
			 * further modifications do not impact the instance stored in the
			 * custom inventory
			 */
			else
				items.put(slot.getIndex(), item.clone());

			/*
			 * Equip vanilla items
			 */
		} else
			slot.getType().getVanillaSlotHandler().equip(player, item);
	}

	public ItemStack getItem(CustomSlot slot) {
		return getItem(slot.getIndex());
	}

	public ItemStack getItem(int slot) {
		return items.containsKey(slot) ? items.get(slot) : null;
	}

	@Override
	public Collection<ItemStack> getExtraItems() {
		return items.values();
	}

	public Set<Integer> getFilledSlotKeys() {
		return items.keySet();
	}

	// heavy performance, use above instead
	public Set<CustomSlot> getFilledSlots() {
		return items.keySet().stream().map(id -> MMOInventory.plugin.getSlotManager().get(id)).collect(Collectors.toSet());
	}

	@Override
	public void whenSaved() {
		ConfigFile config = new ConfigFile(MMOInventory.plugin, "/userdata", player.getUniqueId().toString());

		// Important: CLEAR current data
		config.getConfig().set("inventory", null);

		try {
			for (int index : items.keySet()) {
				ItemStack item = items.get(index);
				config.getConfig().set("inventory." + index, isAir(item) ? null : item);
			}
			config.save();
		} catch (Exception exception) {
			MMOInventory.plugin.getLogger().log(Level.SEVERE, "Could not save inventory of " + player.getName() + ": " + exception.getMessage());
		}
	}

	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
