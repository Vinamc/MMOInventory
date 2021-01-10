package net.Indyuce.inventory.comp;

import net.Indyuce.inventory.api.LineConfig;
import net.Indyuce.inventory.api.inventory.InventoryHandler;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotRestriction;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Bukkit.getServer;

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

    /**
     * Avoids an infinite loop of "Unique" items checking if themselves
     * have passed this condition when checking if they pass it.
     */
    boolean verifying = false;
    public boolean isVerifying() { return verifying; }

    @Override
    public boolean isVerified(InventoryHandler provider, CustomSlot slot, ItemStack item) {
        if (!isEnabled()) { return true; }
        if (isVerifying()) { return false; }
        verifying = true;

        String set = NBTItem.get(item).getString("MMOITEMS_ACCESSORY_SET");

        // Get Equipped Items
        for (ItemStack invItem : provider.getExtraItemsUnverified(slot.getIndex())) {

            // Same MMOItem?
            boolean sameItem = getStringMMOItem(invItem).equalsIgnoreCase(getStringMMOItem(item));
            boolean sameSet = (set != null) && (set.length() > 0) && set.equalsIgnoreCase(NBTItem.get(invItem).getString("MMOITEMS_ACCESSORY_SET"));

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

    private String getStringMMOItem(ItemStack item) {
        NBTItem nbtItem = NBTItem.get(item);
        return nbtItem.getString("MMOITEMS_ITEM_ID") + "." + nbtItem.getString("MMOITEMS_ITEM_TYPE");
    }
}
