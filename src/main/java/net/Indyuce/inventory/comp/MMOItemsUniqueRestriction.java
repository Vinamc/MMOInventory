package net.Indyuce.inventory.comp;

import net.Indyuce.inventory.api.LineConfig;
import net.Indyuce.inventory.api.inventory.InventoryHandler;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotRestriction;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.inventory.ItemStack;

public class MMOItemsUniqueRestriction extends SlotRestriction {

    private final boolean enabled;

    /**
     * Used to register item application restrictions with stats.
     *
     * @param config
     */
    public MMOItemsUniqueRestriction(LineConfig config) {
        super(config);
        config.validate("enabled");
        enabled = config.getBoolean("enabled");
    }

    @Override
    public boolean isVerified(InventoryHandler provider, CustomSlot slot, ItemStack item) {
        if (!isEnabled())
            return true;
        String set = NBTItem.get(item).getString("MMOITEMS_ACCESSORY_SET");
        for (ItemStack invItem : provider.getExtraItems()) {
            if (getStringMMOItem(invItem).equalsIgnoreCase(getStringMMOItem(item)) ||
                    set != null && set.equalsIgnoreCase(NBTItem.get(invItem).getString("MMOITEMS_ACCESSORY_SET")))
                return false;
        }
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private String getStringMMOItem(ItemStack item) {
        NBTItem nbtItem = NBTItem.get(item);
        return nbtItem.getString("MMOITEMS_ITEM_ID") + "." + nbtItem.getString("MMOITEMS_ITEM_TYPE");
    }
}
