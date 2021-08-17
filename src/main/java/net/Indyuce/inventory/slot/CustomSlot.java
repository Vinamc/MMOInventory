package net.Indyuce.inventory.slot;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.inventory.InventoryHandler;
import net.Indyuce.inventory.util.LineConfig;
import net.Indyuce.inventory.version.ItemTag;
import net.Indyuce.inventory.version.NBTItem;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CustomSlot {
	private final String id, name;
	private final SlotType type;
	private final int slot;
	private final ItemStack item;

	/**
	 * Slot restrictions used by external plugins to apply type or class
	 * restrictions for instance
	 */
	private final List<SlotRestriction> restrictions = new ArrayList<>();

	/**
	 * Used to register custom RPG inventory slots from other plugins
	 *
	 * @param id
	 *            The custom slot id (CHESTPLATE)
	 * @param name
	 *            The custom slot name ("Chestplate")
	 * @param type
	 *            The slot type, use the corresponding type for vanilla slots,
	 *            ACCESSORY for custom RPG slots, or FILL for filler items
	 * @param slot
	 *            The GUI slot that will be used to display the current item in
	 *            /rpginv
	 * @param item
	 *            The itemstack used to indicate the custom slot in the GUI
	 */
	public CustomSlot(String id, String name, SlotType type, int slot, ItemStack item) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.item = item;

		// This makes sure any fill slot does not interfere with other slots
		this.slot = type == SlotType.FILL ? -1 : slot;
	}

	public CustomSlot(ConfigurationSection config) {
		Validate.notNull(config, "Could not read slot config");
		id = config.getName().toLowerCase().replace("_", "-").replace(" ", "-");

		Validate.notNull(config.getString("type"), "Could not read slot type");
		type = SlotType.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		slot = type == SlotType.FILL ? -1 : config.getInt("slot");

		// cache slot item
		Validate.notNull(name = config.getString("name"), "Could not read slot name");
		Validate.notNull(config.getStringList("lore"), "Could not read slot lore");
		Validate.notNull(config.getString("material"), "Could not read material");
		int model = config.contains("durability") ? config.getInt("durability") : config.getInt("custom-model-data");
		ItemStack item = MMOInventory.plugin.getVersionWrapper()
				.getModelItem(Material.valueOf(config.getString("material").toUpperCase().replace("-", "_").replace(" ", "_")), model);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		meta.addItemFlags(ItemFlag.values());
		List<String> lore = new ArrayList<>();
		for (String line : config.getStringList("lore"))
			lore.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', line));
		meta.setLore(lore);
		item.setItemMeta(meta);

		// load slot restrictions
		if (config.contains("restrictions"))
			for (String key : config.getStringList("restrictions"))
				restrictions.add(MMOInventory.plugin.getSlotManager().readRestriction(new LineConfig(key)));

		NBTItem nbt = MMOInventory.plugin.getVersionWrapper().getNBTItem(item).addTag(new ItemTag("MMOInventoryGuiItem", getId()),
				new ItemTag("Unbreakable", true));
		this.item = nbt.toItem();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return slot;
	}

	public SlotType getType() {
		return type;
	}

	public ItemStack getItem() {
		return item;
	}

	/**
	 * @param player Data of the player equipping the item
	 * @param item   The item being equipped in the slot
	 * @return If the item can be equipped in that slot. This only checks for
	 *         custom restrictions and NOT for vanilla slot based restrictions.
	 *         See {@link #canHost(InventoryHandler, NBTItem)}
	 */
	public boolean checkSlotRestrictions(InventoryHandler player, NBTItem item) {
		for (SlotRestriction restriction : restrictions)
			if (!restriction.isVerified(player, this, item))
				return false;

		return true;
	}

	/**
	 * The best method to check if an item can host another item. This can
	 * be used for BOTH vanilla slot and custom slots!
	 *
	 * @param player Data of the player equipping the item
	 * @param item   The item being equipped in the slot
	 * @return If the item can be equipped in that slot. This checks for
	 *         both custom AND vanilla slot restrictions
	 */
	public boolean canHost(InventoryHandler player, NBTItem item) {
		return getType().isCustom() ? checkSlotRestrictions(player, item) : getType().getVanillaSlotHandler().canEquip(item.getItem());
	}
}
