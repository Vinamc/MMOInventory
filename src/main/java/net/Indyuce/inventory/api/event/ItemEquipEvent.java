package net.Indyuce.inventory.api.event;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.api.slot.CustomSlot;

public class ItemEquipEvent extends PlayerEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final ItemStack item;
	private final CustomSlot slot;

	private boolean cancelled = false;

	/**
	 * Event called whenever a player equips an item in a custom or vanilla slot
	 * when he has the custom inventory opened
	 * 
	 * @param player
	 *            Playing equiping the item
	 * @param item
	 *            Item being equipped in a custom slot, or null if the player is
	 *            unequipping it
	 * @param slot
	 *            The slot the item is equipped in
	 */
	public ItemEquipEvent(Player player, @Nullable ItemStack item, CustomSlot slot) {
		super(player);

		this.item = item;
		this.slot = slot;
	}

	public CustomSlot getSlot() {
		return slot;
	}

	public @Nullable ItemStack getItem() {
		return item;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean bool) {
		cancelled = bool;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
