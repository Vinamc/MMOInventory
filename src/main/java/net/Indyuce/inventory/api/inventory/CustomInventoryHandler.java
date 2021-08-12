package net.Indyuce.inventory.api.inventory;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.ConfigFile;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotType;
import net.Indyuce.inventory.sql.SQLUserdata;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CustomInventoryHandler extends InventoryHandler {

	/*
	 * Items are stored inside a map with their corresponding slot because it is
	 * much easier to access every item at the same time. It also makes items
	 * accessible and EDITABLE by any other plugins.
	 */
	public final Map<Integer, ItemStack> items = new HashMap<>();

	/**
	 * Used when MMOInventory utilizes the custom inventory GUI
	 */
	public CustomInventoryHandler(Player player) {
		super(player);

		if(MMOInventory.plugin.getSQLManager().isEnabled()) loadSQL();
		else load();
	}
	
	private void load() {
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
	
	private void loadSQL() {
		SQLUserdata data = MMOInventory.plugin.getSQLManager().getUserData(player.getUniqueId().toString());
		for(Entry<Integer, ItemStack> entry : data.get())
			items.put(entry.getKey(), entry.getValue());
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
		return items.get(slot);
	}

	public boolean hasItem(CustomSlot slot) {
		return items.containsKey(slot);
	}

	@Override
	public Collection<ItemStack> getExtraItems() {
		return items.values();
	}

	@Override
	public Collection<ItemStack> getExtraItemsUnverified(Integer... excluded) {

		// Don't return the excluded values tho
		Collection<ItemStack> ret = items.values();

		// Remove excluded
		for (Integer ex : excluded) {

			// Get
			ItemStack value = items.get(ex);

			// Remove
			ret.remove(value);
		}

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

	@Override
	public void whenSavedSQL() {
		MMOInventory.plugin.getSQLManager().save(player.getUniqueId().toString(), items.entrySet());
	}

	private boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}
}
