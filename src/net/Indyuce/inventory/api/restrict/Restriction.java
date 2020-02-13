package net.Indyuce.inventory.api.restrict;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.api.InventoryData;
import net.Indyuce.inventory.api.LineConfig;
import net.Indyuce.inventory.api.slot.CustomSlot;

public abstract class Restriction {
	private final LineConfig config;

	public Restriction(LineConfig config) {
		this.config = config;
	}

	public LineConfig getConfig() {
		return config;
	}

	/*
	 * called when the player tries to equip an item in a specific slot
	 */
	public abstract boolean isVerified(Player player, InventoryData data, CustomSlot slot, ItemStack item);
}
