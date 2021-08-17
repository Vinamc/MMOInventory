package net.Indyuce.inventory;

import net.Indyuce.inventory.command.MMOInventoryCommand;
import net.Indyuce.inventory.command.MMOInventoryCompletion;
import net.Indyuce.inventory.compat.InventoryUpdater;
import net.Indyuce.inventory.compat.mmoitems.MMOItemsCompatibility;
import net.Indyuce.inventory.inventory.SimpleInventoryHandler;
import net.Indyuce.inventory.listener.*;
import net.Indyuce.inventory.manager.DataManager;
import net.Indyuce.inventory.manager.SlotManager;
import net.Indyuce.inventory.manager.YamlDataManager;
import net.Indyuce.inventory.manager.sql.SQLDataManager;
import net.Indyuce.inventory.util.ConfigFile;
import net.Indyuce.inventory.version.ServerVersion;
import net.Indyuce.inventory.version.wrapper.VersionWrapper;
import net.Indyuce.inventory.version.wrapper.VersionWrapper_Reflection;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class MMOInventory extends JavaPlugin implements Listener {
	public static MMOInventory plugin;

	private final SlotManager slotManager = new SlotManager();

	/**
	 * See {@link InventoryUpdater} for explanation. This is
	 * the list of all the plugins which require inventory updates.
	 */
	private final List<InventoryUpdater> inventoryUpdaters = new ArrayList<>();

	private DataManager dataManager;
	private ServerVersion version;
	private VersionWrapper versionWrapper;
	private ConfigFile language;

	// Cached config options
	public int inventorySlots;

	public void onLoad() {
		plugin = this;
	}

	public void onEnable() {

		try {
			version = new ServerVersion(Bukkit.getServer().getClass());
			getLogger().log(Level.INFO, "Detected Bukkit Version: " + version.toString());
			versionWrapper = (VersionWrapper) Class.forName("net.Indyuce.inventory.version.wrapper.VersionWrapper_" + version.toString().substring(1))
					.newInstance();
		} catch (Exception e) {
			getLogger().log(Level.INFO, "Your server version is handled via reflection");
			versionWrapper = new VersionWrapper_Reflection();
		}

		saveDefaultConfig();
		saveDefaultFile("language");
		saveDefaultFile("items");
		reload();

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(new GuiListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

		// MySQL or YAML
		dataManager = getConfig().getBoolean("mysql.enabled") ? new SQLDataManager() : new YamlDataManager();

		if (getConfig().getBoolean("resource-pack.enabled"))
			Bukkit.getServer().getPluginManager().registerEvents(new ResourcePack(getConfig().getConfigurationSection("resource-pack")), this);

		if (getConfig().getBoolean("no-custom-inventory")) {
			dataManager.setInventoryProvider(SimpleInventoryHandler::new);
			Bukkit.getPluginManager().registerEvents(new NoCustomInventory(), this);
		} else {

			if (getConfig().getBoolean("save-on-leave"))
				Bukkit.getPluginManager().registerEvents(new SaveOnLeave(), this);

			if (getConfig().getBoolean("inventory-button.enabled"))
				Bukkit.getPluginManager().registerEvents(new InventoryButtonListener(getConfig().getConfigurationSection("inventory-button")), this);

			if (getConfig().getBoolean("drop-on-death"))
				Bukkit.getServer().getPluginManager().registerEvents(new DeathDrops(), this);
		}

		getCommand("mmoinventory").setExecutor(new MMOInventoryCommand());
		getCommand("mmoinventory").setTabCompleter(new MMOInventoryCompletion());

		// /reload friendly
		Bukkit.getOnlinePlayers().forEach(dataManager::setupData);

		if (Bukkit.getPluginManager().getPlugin("MMOItems") != null) {
			new MMOItemsCompatibility();
			getLogger().log(Level.INFO, "Hooked onto MMOItems");
		}
	}

	public void onDisable() {
		dataManager.save();
	}

	public void reload() {
		reloadConfig();
		language = new ConfigFile("language");
		slotManager.reload();

		try {
			inventorySlots = getConfig().getInt("inventory-slots");
			Validate.isTrue(inventorySlots > 0 && inventorySlots < 55, "Number must be greater than 9 and lower than 54");
			Validate.isTrue(inventorySlots % 9 == 0, "Number must be a multiple of 9");

		} catch (IllegalArgumentException exception) {
			inventorySlots = 36;
			getLogger().log(Level.WARNING, "Invalid inventory slot number: " + exception.getMessage());
		}
	}

	public VersionWrapper getVersionWrapper() {
		return versionWrapper;
	}

	public DataManager getDataManager() {
		return dataManager;
	}

	public SlotManager getSlotManager() {
		return slotManager;
	}

	public ServerVersion getVersion() {
		return version;
	}

	public void registerInventoryUpdater(InventoryUpdater updater) {
		Validate.notNull(updater, "Updater cannot be null");

		inventoryUpdaters.add(updater);
	}

	/**
	 * Iterates through all registered {@link InventoryUpdater} and
	 * updates the player for every plugin that needs it.
	 *
	 * @param player Player which inventory requires an update
	 */
	public void updateInventory(Player player) {
		for (InventoryUpdater updater : inventoryUpdaters)
			updater.updateInventory(player);
	}

	public String getTranslation(String path) {
		return ChatColor.translateAlternateColorCodes('&', language.getConfig().getString(path));
	}

	private void saveDefaultFile(String path) {
		try {
			File file = new File(getDataFolder(), path + ".yml");
			if (!file.exists())
				Files.copy(getResource("default/" + path + ".yml"), file.getAbsoluteFile().toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
