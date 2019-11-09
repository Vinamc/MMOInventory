package net.Indyuce.inventory.api.slot;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class CustomSlot {

	private final String id, name;
	private final SlotType type;
	private final int slot;
	private final ItemStack item;

	private String mmoitemType;

	/*
	 * may be used to register custom slots using other plugins
	 */
	public CustomSlot(String id, String name, SlotType type, int slot, ItemStack item, String mmoitemType) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.slot = slot;
		this.item = item;
		this.mmoitemType = mmoitemType;
	}

	public CustomSlot(ConfigurationSection config) {
		Validate.notNull(config, "Could not read slot config");
		id = config.getName().toLowerCase().replace("_", "-").replace(" ", "-");

		Validate.notNull(config.getString("type"), "Could not read slot type");
		type = SlotType.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		slot = config.getInt("slot");

		if (type.isCustom())
			Validate.notNull(mmoitemType = config.getString("mmoitems-type"), "Could not read slot MMOItems type");

		/*
		 * cache slot item
		 */
		Validate.notNull(name = config.getString("name"), "Could not read slot name");
		Validate.notNull(config.getStringList("lore"), "Could not read slot lore");

		Validate.notNull(config.getString("material"), "Could not read material");
		ItemStack item = new ItemStack(Material.valueOf(config.getString("material").toUpperCase().replace("-", "_").replace(" ", "_")));
		ItemMeta meta = item.getItemMeta();
		if (meta instanceof Damageable)
			((Damageable) meta).setDamage((short) config.getInt("durability"));
		meta.setUnbreakable(true);
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		meta.addItemFlags(ItemFlag.values());
		List<String> lore = new ArrayList<>();
		for (String line : config.getStringList("lore"))
			lore.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', line));
		meta.setLore(lore);
		item.setItemMeta(meta);

		NBTItem nbt = MMOItems.plugin.getNMS().getNBTItem(item).addTag(new ItemTag("inventoryItem", getId()));
		if (MMOItems.plugin.getVersion().isStrictlyHigher(1, 13))
			nbt.addTag(new ItemTag("CustomModelData", config.getInt("durability")));
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

	public boolean matches(Type type) {
		return mmoitemType.equalsIgnoreCase(type.getId());
	}

	public String getMMOItemType() {
		return mmoitemType;
	}

	public ItemStack getItem() {
		return item;
	}

	public boolean canEquip(ItemStack item) {
		if (!getType().isCustom())
			return type.getVanillaSlotHandler().canEquip(item);

		Type type = NBTItem.get(item).getType();
		return type != null && matches(type);
	}
}
