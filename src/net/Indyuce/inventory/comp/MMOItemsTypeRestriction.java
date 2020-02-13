package net.Indyuce.inventory.comp;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.api.InventoryData;
import net.Indyuce.inventory.api.LineConfig;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotRestriction;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMOItemsTypeRestriction extends SlotRestriction {
	private final Type type;

	public MMOItemsTypeRestriction(LineConfig config) {
		super(config);

		config.validate("type");
		String format = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTypes().has(format));
		type = MMOItems.plugin.getTypes().get(format);
	}

	@Override
	public boolean isVerified(Player player, InventoryData data, CustomSlot slot, ItemStack item) {
		if (!slot.getType().isCustom())
			return item != null && slot.getType().getVanillaSlotHandler().canEquip(item);

		Type type = NBTItem.get(item).getType();
		return type != null && this.type.equals(type);
	}

	public Type getType() {
		return type;
	}
}
