package net.Indyuce.inventory.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.Indyuce.inventory.api.inventory.CustomInventoryHandler;
import net.Indyuce.inventory.api.inventory.InventoryHandler;

public class DataManager {
	private final Map<UUID, InventoryHandler> inventories = new HashMap<>();

	/**
	 * Function used to generate an inventoryProvider instance given a player.
	 * There are two types of inventoryProviders: Complex, which are used when
	 * the custom inventory is used, and Simple when the 'no-custom-inventory'
	 * is toggled on
	 */
	private Function<Player, InventoryHandler> load = (player -> new CustomInventoryHandler(player));

	/**
	 * Used by the 'no-custom-inventory' config option
	 * 
	 * @param load
	 *            A function which takes a player as input and returns a new
	 *            inventoryProvider corresponding to the player
	 */
	public void setInventoryProvider(Function<Player, InventoryHandler> load) {
		Validate.notNull(load, "Function cannot be null");

		this.load = load;
	}

	public InventoryHandler getInventory(OfflinePlayer player) {
		return inventories.get(player.getUniqueId());
	}

	public InventoryHandler getInventory(UUID uuid) {
		return inventories.get(uuid);
	}

	public void setupData(Player player) {

		/*
		 * Setup inventory data if not setup
		 */
		if (!inventories.containsKey(player.getUniqueId()))
			inventories.put(player.getUniqueId(), load.apply(player));

		/*
		 * Or else refresh player instance
		 */
		else
			getInventory(player.getUniqueId()).setPlayer(player);
	}

	public void unloadData(OfflinePlayer player) {
		inventories.remove(player.getUniqueId());
	}

	public void unloadData(UUID uuid) {
		inventories.remove(uuid);
	}

	public Collection<InventoryHandler> getLoaded() {
		return inventories.values();
	}
}
