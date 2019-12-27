package net.Indyuce.inventory.comp;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotType;
import net.Indyuce.mmoitems.api.Type;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMOItemsCustomSlot extends CustomSlot {
	private String mmoitemsType;

	public MMOItemsCustomSlot(ConfigurationSection config) {
		super(config);

		if (getType().isCustom())
			Validate.notNull(mmoitemsType = config.getString("mmoitems-type"), "Could not read slot MMOItems type");
	}

	public MMOItemsCustomSlot(String id, String name, SlotType type, int slot, ItemStack item, String mmoitemsType) {
		super(id, name, type, slot, item);

		this.mmoitemsType = mmoitemsType;
	}

	public String getMMOItemType() {
		return mmoitemsType;
	}

	@Override
	public boolean canEquip(ItemStack item) {
		if (!getType().isCustom())
			return item != null && getType().getVanillaSlotHandler().canEquip(item);

		Type type = NBTItem.get(item).getType();
		return type != null && matches(type);
	}

	public boolean matches(Type type) {
		return mmoitemsType.equalsIgnoreCase(type.getId());
	}
}
