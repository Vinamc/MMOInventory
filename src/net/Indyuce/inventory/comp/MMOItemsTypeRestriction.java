package net.Indyuce.inventory.comp;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.api.InventoryData;
import net.Indyuce.inventory.api.LineConfig;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotRestriction;
import net.Indyuce.mmoitems.api.Type;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMOItemsTypeRestriction extends SlotRestriction {

	/*
	 * forced to save the mmoitems type as a string and not a type instance
	 * because the TypeManager has not been initialized yet
	 */
	private final String id;

	public MMOItemsTypeRestriction(LineConfig config) {
		super(config);

		config.validate("type");
		id = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
	}

	@Override
	public boolean isVerified(Player player, InventoryData data, CustomSlot slot, ItemStack item) {
		if (!slot.getType().isCustom())
			return item != null && slot.getType().getVanillaSlotHandler().canEquip(item);

		Type type = NBTItem.get(item).getType();
		return type != null && id.equals(type.getId());
	}

	public String getType() {
		return id;
	}
}
