package net.Indyuce.inventory.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.ConfigFile;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotType;

public class SlotManager {

	/*
	 * custom slot instances saved using bukkit inventory slot index
	 */
	public final Map<Integer, CustomSlot> slots = new HashMap<>();

	/*
	 * used to fill up inventory space
	 */
	private CustomSlot fill = new CustomSlot("FILL", "", SlotType.FILL, -1, new ItemStack(Material.AIR), "");

	public void register(CustomSlot slot) {

		if (slots.containsKey(slot.getIndex())) {
			MMOInventory.plugin.getLogger().log(Level.WARNING, "Attempted to register two slots (" + slot.getName() + ") with the same inventory index.");
			return;
		}

		slots.put(slot.getIndex(), slot);
		if (slot.getType() == SlotType.FILL)
			fill = slot;
	}

	public void unregister(int index) {
		slots.remove(index);
	}

	public CustomSlot get(int index) {
		return slots.containsKey(index) ? slots.get(index) : null;
	}

	public Collection<CustomSlot> getLoaded() {
		return slots.values();
	}

	public CustomSlot getFiller() {
		return fill;
	}

	public void reload() {
		slots.clear();

		FileConfiguration config = new ConfigFile("items").getConfig();
		for (String key : config.getKeys(false))
			try {
				ConfigurationSection section = config.getConfigurationSection(key);
				Validate.notNull(section, "Could not read config section");

				register(new CustomSlot(section));
			} catch (IllegalArgumentException exception) {
				MMOInventory.plugin.getLogger().log(Level.WARNING, "Could not load slot " + key + ": " + exception.getMessage());
			}

		MMOInventory.plugin.getLogger().log(Level.INFO, "Successfully registered " + slots.size() + " inventory slots.");
	}

	public Set<CustomSlot> getCustomSlots() {
		return getLoaded().stream().filter(slot -> slot.getType().isCustom()).collect(Collectors.toSet());
	}
}
