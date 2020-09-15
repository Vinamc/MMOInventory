package net.Indyuce.inventory.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;

import net.Indyuce.inventory.MMOInventory;

public class SQLManager {
	private final boolean enabled;
	private final MySQLConfig config;
	private Connection connection;
	private final Gson gson = new Gson();

	public SQLManager() {
		ConfigurationSection section = MMOInventory.plugin.getConfig().getConfigurationSection("mysql");
		enabled = section.getBoolean("enabled", false);
		config = new MySQLConfig(section);
	}

	public void load() {
		if (!enabled) return;
		initialize();

		executeUpdate("CREATE TABLE IF NOT EXISTS mmoinv_players (uuid VARCHAR(36) NOT NULL,"
				+ "id INT(11) NOT NULL AUTO_INCREMENT,PRIMARY KEY (id),UNIQUE KEY (uuid));");
		executeUpdate("CREATE TABLE IF NOT EXISTS mmoinv_data (id INT(11) NOT NULL DEFAULT -1,"
				+ "slot_index INT(11) NOT NULL DEFAULT 0,stack JSON,UNIQUE KEY (id));");
	}

	private void initialize() {
		try {
			connection = DriverManager.getConnection(config.getConnectionString(), config.getUser(),
					config.getPassword());
		} catch (SQLException exception) {
			throw new IllegalArgumentException("Could not initialize MySQL support: " + exception.getMessage());
		}
	}

	public ResultSet getResult(final String sql) {
		try {
			return getConnection().prepareStatement(sql).executeQuery();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public void executeUpdate(final String sql) {
		try {
			getConnection().prepareStatement(sql).executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}

	private Connection getConnection() {
		try {
			if (connection.isClosed())
				initialize();
		} catch (SQLException e) {
			initialize();
		}

		return connection;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public class MySQLConfig {
		private final String database, hostname, userid, password, flags;
		private final int port;

		public MySQLConfig(ConfigurationSection config) {
			database = config.getString("database", "minecraft");
			hostname = config.getString("host", "localhost");
			port = config.getInt("port", 3306);
			userid = config.getString("user", "mmolover");
			password = config.getString("pass", "ILoveAria");
			flags = config.getString("flags", "?allowReconnect=true&useSSL=false");
		}

		public String getConnectionString() {
			return "jdbc:mysql://" + hostname + ":" + port + "/" + database + flags;
		}

		public String getUser() {
			return userid;
		}

		public String getPassword() {
			return password;
		}
	}

	public int getID(final String uuid) {
		executeUpdate("INSERT INTO mmoinv_players (uuid) SELECT * FROM (SELECT '" + uuid + "') AS tmp "
				+ "WHERE NOT EXISTS (SELECT uuid FROM mmoinv_players WHERE uuid = '" + uuid + "') LIMIT 1;");
		ResultSet result = getResult("SELECT id FROM mmoinv_players WHERE uuid = '" + uuid + "'");
		try {
			if (result.first())
				return result.getInt("id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public SQLUserdata getUserData(final String uuid) {
		final SQLUserdata data = new SQLUserdata();
		final int id = getID(uuid);
		if(id == -1) {
			MMOInventory.plugin.getLogger().severe("Couldn't get ID for '" + uuid + "'");
			return data;
		}
		try {
			ResultSet result = getResult("SELECT slot_index,stack FROM mmoinv_data WHERE id = '" + id + "'");
			while(result.next()) {
				ItemStack stack = gson.fromJson(result.getString("stack"), ItemStack.class);
				data.put(result.getInt("slot_index"), stack);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return data;
	}

	public void save(final String uuid, final Set<Entry<Integer, ItemStack>> inventory) {
		final int id = getID(uuid);
		executeUpdate("DELETE FROM mmoinv_data WHERE id = '" + id + "'");
		StringBuilder builder = new StringBuilder();
		for (Entry<Integer, ItemStack> entry : inventory) {
			String stack = gson.toJson(entry.getValue());
			builder.append("(" + id + ", " + entry.getKey() + ", '" + stack + "')");
			builder.append(", ");
		}
		builder.setLength(builder.length() - 2); //gets rid of the last '", "'
		executeUpdate("INSERT INTO mmoinv_data (id,slot_index,stack) VALUES " + builder.toString() + ";");
	}
}
