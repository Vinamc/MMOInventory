package net.Indyuce.inventory.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import net.Indyuce.inventory.gui.PlayerInventoryView;

public class GuiListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void a(InventoryClickEvent event) {
		if (!event.isCancelled())
			if (event.getInventory().getHolder() instanceof PlayerInventoryView)
				((PlayerInventoryView) event.getInventory().getHolder()).whenClicked(event);
	}

	@EventHandler
	public void b(InventoryCloseEvent event) {
		if (event.getInventory().getHolder() instanceof PlayerInventoryView)
			((PlayerInventoryView) event.getInventory().getHolder()).whenClosed(event);
	}
}
