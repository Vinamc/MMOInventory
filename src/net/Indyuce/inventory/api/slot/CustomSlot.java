package net.Indyuce.inventory.api.slot;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.api.LineConfig;
import net.Indyuce.inventory.api.NBTItem;
import net.Indyuce.inventory.api.restrict.Restriction;
import net.Indyuce.inventory.version.ItemTag;

public class CustomSlot {

	private final String id, name;
	private final SlotType type;
	private final int slot;
	private final ItemStack item;

	/*
	 * slot restrictions used by external plugins to apply type, class
	 * restrictions, etc.
	 */
	private final List<Restriction> restrictions = new ArrayList<>();

	/*
	 * may be used to register custom slots using other plugins
	 */
	public CustomSlot(String id, String name, SlotType type, int slot, ItemStack item) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.slot = slot;
		this.item = item;
	}

	public CustomSlot(ConfigurationSection config) {
		Validate.notNull(config, "Could not read slot config");
		id = config.getName().toLowerCase().replace("_", "-").replace(" ", "-");

		Validate.notNull(config.getString("type"), "Could not read slot type");
		type = SlotType.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		slot = config.getInt("slot");

		// cache slot item
		Validate.notNull(name = config.getString("name"), "Could not read slot name");
		Validate.notNull(config.getStringList("lore"), "Could not read slot lore");
		Validate.notNull(config.getString("material"), "Could not read material");
		int model = config.contains("durability") ? config.getInt("durability") : config.getInt("custom-model-data");
		ItemStack item = MMOInventory.plugin.getVersionWrapper().getModelItem(Material.valueOf(config.getString("material").toUpperCase().replace("-", "_").replace(" ", "_")), model);
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

		NBTItem nbt = MMOInventory.plugin.getVersionWrapper().getNBTItem(item).addTag(new ItemTag("inventoryItem", getId()), new ItemTag("Unbreakable", true));
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

	public boolean isValid() {
		return type != SlotType.FILL;
	}

	public SlotType getType() {
		return type;
	}

	public ItemStack getItem() {
		return item;
	}

	public List<Restriction> getRestrictions() {
		return restrictions;
	}
}
