package net.Indyuce.inventory.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.UUID;

public abstract class InventoryHandler {
	private final UUID uuid;

	/**
	 * Player instance is not final because it needs to be updated every time
	 * the player joins; this instance is used to equip vanilla items
	 */
	protected Player player;

	public InventoryHandler(Player player) {
		this.player = player;
		this.uuid = player.getUniqueId();
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public Player getPlayer() {
		return player;
	}

	public void updatePlayer(Player player) {
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
}
