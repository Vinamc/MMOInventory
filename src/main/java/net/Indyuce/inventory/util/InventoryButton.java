package net.Indyuce.inventory.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.version.ItemTag;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.stream.Collectors;

public class InventoryButton {
	private final ItemStack item;

	/**
	 * Used to read an item icon from the config file. It can be a textured
	 * player head using the 'texture' config key, a modeled item using 'model'.
	 * Using 'material' you can choose the item material.
	 * 
	 * @param config
	 *            Config to read from
	 */
	public InventoryButton(ConfigurationSection config) {
		Validate.notNull(config, "Config cannot be null");

		Validate.isTrue(config.contains("material"), "Could not find item material");
		this.item = new ItemStack(Material.valueOf(config.getString("material")));

		ItemMeta meta = item.getItemMeta();

		// Display name
		if (config.contains("name"))
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("name")));

		// Lore
		if (config.contains("lore"))
			meta.setLore(config.getStringList("lore").stream().map(str -> ChatColor.translateAlternateColorCodes('&', str))
					.collect(Collectors.toList()));

		// Skull texture
		if (config.contains("texture") && item.getType() == Material.PLAYER_HEAD)
			try {
				GameProfile profile = new GameProfile(UUID.randomUUID(), null);
				profile.getProperties().put("textures", new Property("textures", config.getString("texture")));
				Field profileField = meta.getClass().getDeclaredField("profile");
				profileField.setAccessible(true);
				profileField.set(meta, profile);
			} catch (NoSuchFieldException | IllegalAccessException exception) {
				throw new IllegalArgumentException(exception.getMessage());
			}

		item.setItemMeta(meta);

		// Apply custom model data
		if (config.contains("model"))
			item.setItemMeta(MMOInventory.plugin.getVersionWrapper().getModelItem(item.getType(), config.getInt("model")).getItemMeta());

		// Apply correct NBTtag
		item.setItemMeta(MMOInventory.plugin.getVersionWrapper().getNBTItem(item).addTag(new ItemTag("MMOInventoryButton", true))
				.toItem().getItemMeta());
	}

	public ItemStack getItem() {
		return item;
	}
}
