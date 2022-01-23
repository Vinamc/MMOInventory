package net.Indyuce.inventory.slot.restriction;

import net.Indyuce.inventory.inventory.InventoryHandler;
import net.Indyuce.inventory.slot.CustomSlot;
import net.Indyuce.inventory.util.LineConfig;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * This easy implementation of a slot restriction can be used
 * when LoreAttributesRecoded is installed to easily implement
 * item types.
 * <p>
 * Item types are basically a small colored lore tag like '&cSword'
 * at the beginning of the item lore (it can be positionned anywhere).
 * <p>
 * Checking if the item has a specific item type is basically looking
 * for that tag in the item lore.
 */
public class LoreTagRestriction extends SlotRestriction {
    private final String loreLine;

    public LoreTagRestriction(LineConfig config) {
        config.validate("line");
        loreLine = ChatColor.translateAlternateColorCodes('&', config.getString("line"));
    }

    @Override
    public boolean isVerified(InventoryHandler provider, CustomSlot slot, ItemStack item) {
        if (!item.hasItemMeta())
            return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore())
            return false;

        for (String checked : meta.getLore())
            if (checked.equals(loreLine))
                return true;

        return false;
    }
}
