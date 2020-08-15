package net.Indyuce.inventory.version.wrapper;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.api.NBTItem;

public interface VersionWrapper {
	public NBTItem getNBTItem(ItemStack item);

	public ItemStack getModelItem(Material material, int model);

	public boolean isHelmet(Material material);
}
