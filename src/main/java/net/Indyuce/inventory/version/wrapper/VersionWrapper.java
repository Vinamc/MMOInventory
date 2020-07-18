package net.Indyuce.inventory.version.wrapper;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.api.NBTItem;

public interface VersionWrapper {
	NBTItem getNBTItem(ItemStack item);

	ItemStack getModelItem(Material material, int model);

	boolean isHelmet(Material material);
}
