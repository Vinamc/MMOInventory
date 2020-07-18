package net.Indyuce.inventory.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.Indyuce.inventory.api.InventoryData;

public class DataManager {
	private final Map<UUID, InventoryData> inventories = new HashMap<>();

	public boolean isInventoryLoaded(OfflinePlayer player) {
		return isInventoryLoaded(player.getUniqueId());
	}

	public boolean isInventoryLoaded(UUID uuid) {
		return inventories.containsKey(uuid);
	}

	public InventoryData getInventory(OfflinePlayer player) {
		return getInventory(player.getUniqueId());
	}

	public InventoryData getInventory(UUID uuid) {
		return inventories.get(uuid);
	}

	public void loadInventory(Player player) {
		inventories.put(player.getUniqueId(), new InventoryData(player));
	}

	public void unload(OfflinePlayer player) {
		unload(player.getUniqueId());
	}

	public void unload(UUID uuid) {
		inventories.remove(uuid);
	}

	public Collection<InventoryData> getLoaded() {
		return inventories.values();
	}
}
