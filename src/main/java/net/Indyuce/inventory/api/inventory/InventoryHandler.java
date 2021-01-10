package net.Indyuce.inventory.api.inventory;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class InventoryHandler {

	/*
	 * Player instance not final because it needs to be updated every time the
	 * player joins; this instance is used to equip vanilla items
	 */
	protected Player player;

	public InventoryHandler(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * @return A collection of all the extra items (vanilla slots put aside) ie
	 *         accessories placed in custom RPG slots
	 */
	public abstract Collection<ItemStack> getExtraItems();

	/**
	 * @return A collection of all the extra items (vanilla slots put aside) ie
	 *         accessories placed in custom RPG slots.
	 *         <p></p>
	 *         Skips verification to avoid an infinite loop when checking for
	 *         the 'unique' requirement where these items check if themselves
	 *         have passed the condition while checking if they pass it.
	 */
	public abstract Collection<ItemStack> getExtraItemsUnverified(Integer... excluded);

	/**
	 * Called when the plugin is disabling/player is leaving. Should be used to
	 * save the data in a config file if needed
	 */
	public abstract void whenSaved();
	public abstract void whenSavedSQL();
}
