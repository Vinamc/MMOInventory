package net.Indyuce.inventory.version.wrapper;

import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;

import net.Indyuce.inventory.api.NBTItem;
import net.Indyuce.inventory.version.ItemTag;
import net.minecraft.server.v1_9_R1.ItemStack;
import net.minecraft.server.v1_9_R1.NBTTagCompound;

public class VersionWrapper_1_9_R1 implements VersionWrapper {

	@Override
	public boolean isHelmet(Material material) {
		return material.name().endsWith("HELMET") || material == Material.PUMPKIN
				|| material.name().equals("SKULL_ITEM");
	}

	@SuppressWarnings("deprecation")
	@Override
	public org.bukkit.inventory.ItemStack getModelItem(Material material, int model) {
		return new org.bukkit.inventory.ItemStack(material, 1, (short) model);
	}

	@Override
	public NBTItem getNBTItem(org.bukkit.inventory.ItemStack item) {
		return new NBTItem_v1_9_R1(item);
	}

	private class NBTItem_v1_9_R1 extends NBTItem {
		private final ItemStack nms;
		private final NBTTagCompound compound;

		public NBTItem_v1_9_R1(org.bukkit.inventory.ItemStack item) {
			super(item);

			nms = CraftItemStack.asNMSCopy(item);
			compound = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
		}

		@Override
		public String getString(String path) {
			return compound.getString(path);
		}

		@Override
		public boolean hasTag(String path) {
			return compound.hasKey(path);
		}

		@Override
		public boolean getBoolean(String path) {
			return compound.getBoolean(path);
		}

		@Override
		public double getDouble(String path) {
			return compound.getDouble(path);
		}

		@Override
		public int getInteger(String path) {
			return compound.getInt(path);
		}

		@Override
		public NBTItem addTag(List<ItemTag> tags) {
			tags.forEach(tag -> {
				if (tag.getValue() instanceof Boolean)
					compound.setBoolean(tag.getPath(), (boolean) tag.getValue());
				else if (tag.getValue() instanceof Double)
					compound.setDouble(tag.getPath(), (double) tag.getValue());
				else if (tag.getValue() instanceof String)
					compound.setString(tag.getPath(), (String) tag.getValue());
				else if (tag.getValue() instanceof Integer)
					compound.setInt(tag.getPath(), (int) tag.getValue());
			});
			return this;
		}

		@Override
		public NBTItem removeTag(String... paths) {
			for (String path : paths)
				compound.remove(path);
			return this;
		}

		@Override
		public Set<String> getTags() {
			return compound.c();
		}

		@Override
		public org.bukkit.inventory.ItemStack toItem() {
			nms.setTag(compound);
			return CraftItemStack.asBukkitCopy(nms);
		}
	}
}
