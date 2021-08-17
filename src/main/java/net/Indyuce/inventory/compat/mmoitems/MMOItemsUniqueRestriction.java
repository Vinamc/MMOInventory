package net.Indyuce.inventory.compat.mmoitems;

import net.Indyuce.inventory.util.LineConfig;
import net.Indyuce.inventory.version.NBTItem;
import net.Indyuce.inventory.inventory.InventoryHandler;
import net.Indyuce.inventory.slot.CustomSlot;
import net.Indyuce.inventory.slot.SlotRestriction;
import org.bukkit.inventory.ItemStack;

public class MMOItemsUniqueRestriction extends SlotRestriction {
    private final boolean enabled;

    /**
     * Used to register item application restrictions with stats.
     *
     * @param config
     */
    public MMOItemsUniqueRestriction(LineConfig config) {
        config.validate("enabled");
        enabled = config.getBoolean("enabled");
    }

    /**
     * Avoids an infinite loop of "Unique" items checking if themselves
     * have passed this condition when checking if they pass it.
     */
    boolean verifying = false;
    public boolean isVerifying() { return verifying; }

    @Override
    public boolean isVerified(InventoryHandler provider, CustomSlot slot, NBTItem item) {
        if (!isEnabled()) { return true; }
        if (isVerifying()) { return false; }
        verifying = true;

        String set = item.getString("MMOITEMS_ACCESSORY_SET");

        // Get Equipped Items
        for (ItemStack invItem : provider.getExtraItemsUnverified(slot.getIndex())) {

            // Same MMOItem?
            NBTItem nbtItem = NBTItem.get(invItem);
            boolean sameItem = getStringMMOItem(nbtItem).equalsIgnoreCase(getStringMMOItem(item));
            boolean sameSet = (set != null) && (set.length() > 0) && set.equalsIgnoreCase(nbtItem.getString("MMOITEMS_ACCESSORY_SET"));

            // Cancel if the mmoitem is the same or the set is the same.
            if (sameItem || sameSet) {

                verifying = false;
                return false;
            }
        }

        verifying = false;
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private String getStringMMOItem(NBTItem item) {
        return item.getString("MMOITEMS_ITEM_ID") + "." + item.getString("MMOITEMS_ITEM_TYPE");
    }
}
