package net.Indyuce.inventory.api.slot;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;

public enum SlotType {

	/**
	 * The vanilla helmet slot
	 */
	HELMET(new VanillaSlotHandler() {

		@Override
		public void equip(Player player, ItemStack item) {
			player.getInventory().setHelmet(item);
		}

		@Override
		public boolean canEquip(ItemStack item) {
			return MMOInventory.plugin.getVersionWrapper().isHelmet(item.getType());
		}

		@Override
		public ItemStack retrieveItem(Player player) {
			return player.getInventory().getHelmet();
		}
	}),

	/**
	 * The vanilla chestplate slot
	 */
	CHESTPLATE(new VanillaSlotHandler() {

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
	}),

	/**
	 * The vanilla leggings slot
	 */
	LEGGINGS(new VanillaSlotHandler() {

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
	}),

	/**
	 * The vanilla boots slot
	 */
	BOOTS(new VanillaSlotHandler() {

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
	}),

	/**
	 * The vanilla off hand slot
	 */
	OFF_HAND(new VanillaSlotHandler() {

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

	/**
	 * Slot type which must be used when registering custom accessory/RPG slots.
	 */
	ACCESSORY(null),

	/**
	 * Slot type used for filler items in the GUI
	 */
	FILL(null);

	private final VanillaSlotHandler vanilla;

	private SlotType(VanillaSlotHandler vanilla) {
		this.vanilla = vanilla;
	}

	/**
	 * @return If the custom slot is NOT a vanilla slot
	 * @deprecated Check if it is SlotType.ACCESSORY instead
	 */
	@Deprecated
	public boolean isCustom() {
		return this == ACCESSORY;
	}

	public VanillaSlotHandler getVanillaSlotHandler() {
		return vanilla;
	}
}
