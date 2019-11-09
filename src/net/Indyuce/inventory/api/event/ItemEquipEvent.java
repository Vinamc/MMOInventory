package net.Indyuce.inventory.api.event;

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

	/*
	 * is NOT called when equipping items in vanilla slots.
	 */
	public ItemEquipEvent(Player player, ItemStack item, CustomSlot slot) {
		super(player);

		this.item = item;
		this.slot = slot;
	}

	public CustomSlot getSlot() {
		return slot;
	}

	public ItemStack getItem() {
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
