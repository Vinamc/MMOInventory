package net.Indyuce.inventory.api.slot;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum SlotType {

	/*
	 * vanilla slots
	 */
	HELMET(new VanillaSlotHandler() {

		@Override
		public void equip(Player player, ItemStack item) {
			player.getInventory().setHelmet(item);
		}

		@Override
		public boolean canEquip(ItemStack item) {
			return item.getType().name().endsWith("_HELMET")
					|| item.getType() == Material.CARVED_PUMPKIN;
		}

		@Override
		public ItemStack retrieveItem(Player player) {
			return player.getInventory().getHelmet();
		}
	}), CHESTPLATE(new VanillaSlotHandler() {

		@Override
		public void equip(Player player, ItemStack item) {
			player.getInventory().setChestplate(item);
		}

		@Override
		public boolean canEquip(ItemStack item) {
			return item.getType().name().endsWith("_CHESTPLATE") || item.getType() == Material.ELYTRA;
		}

		@Override
		public ItemStack retrieveItem(Player player) {
			return player.getInventory().getChestplate();
		}
	}), LEGGINGS(new VanillaSlotHandler() {

		@Override
		public void equip(Player player, ItemStack item) {
			player.getInventory().setLeggings(item);
		}

		@Override
		public boolean canEquip(ItemStack item) {
			return item != null && item.getType().name().endsWith("_LEGGINGS");
		}

		@Override
		public ItemStack retrieveItem(Player player) {
			return player.getInventory().getLeggings();
		}
	}), BOOTS(new VanillaSlotHandler() {

		@Override
		public void equip(Player player, ItemStack item) {
			player.getInventory().setBoots(item);
		}

		@Override
		public boolean canEquip(ItemStack item) {
			return item != null && item.getType().name().endsWith("BOOTS");
		}

		@Override
		public ItemStack retrieveItem(Player player) {
			return player.getInventory().getBoots();
		}
	}), OFF_HAND(new VanillaSlotHandler() {

		@Override
		public void equip(Player player, ItemStack item) {
			player.getInventory().setItemInOffHand(item);
		}

		@Override
		public boolean canEquip(ItemStack item) {
			return true;
		}

		@Override
		public ItemStack retrieveItem(Player player) {
			return player.getInventory().getItemInOffHand();
		}
	}),

	/*
	 * custom types
	 */
	ACCESSORY(null), FILL(null);

	private final VanillaSlotHandler vanilla;

	private SlotType(VanillaSlotHandler vanilla) {
		this.vanilla = vanilla;
	}

	public boolean isCustom() {
		return vanilla == null && this != FILL;
	}

	public VanillaSlotHandler getVanillaSlotHandler() {
		return vanilla;
	}
}
