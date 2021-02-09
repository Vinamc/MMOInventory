package net.Indyuce.inventory.comp;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.inventory.api.LineConfig;
import net.Indyuce.inventory.api.inventory.InventoryHandler;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotRestriction;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.inventory.ItemStack;

public class MMOItemsTypeRestriction extends SlotRestriction {

	/*
	 * Forced to save the mmoitems type as a string and not a type instance
	 * because the TypeManager has not been initialized yet which is fine
	 * because we don't need to get the Type instance for our checks
	 */
	private final String id;

	public MMOItemsTypeRestriction(LineConfig config) {
		super(config);

		config.validate("type");
		id = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
	}

	@Override
	public boolean isVerified(InventoryHandler provider, CustomSlot slot, ItemStack item) {
		Type type = Type.get(NBTItem.get(item).getType());
		return type != null && id.equals(type.getId());
	}

	public String getTypeId() {
		return id;
	}
}
