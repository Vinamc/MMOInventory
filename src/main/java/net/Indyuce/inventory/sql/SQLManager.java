package net.Indyuce.inventory.sql;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;

import net.Indyuce.inventory.MMOInventory;
import net.mmogroup.mmolib.sql.QueryResult;
import net.mmogroup.mmolib.sql.ResultSet;
import net.mmogroup.mmolib.sql.mysql.MySQLConnection;
import net.mmogroup.mmolib.sql.mysql.MySQLConnectionBuilder;
import net.mmogroup.mmolib.sql.pool.ConnectionPool;

public class SQLManager {
	private boolean enabled;
	private MySQLConfig config;
	private ConnectionPool<MySQLConnection> connection;
	private final Gson gson = new Gson();

	public void load(FileConfiguration cfg) {
		if(!cfg.contains("mysql") || !cfg.isConfigurationSection("mysql")) return;
		ConfigurationSection section = cfg.getConfigurationSection("mysql");
		enabled = section.getBoolean("enabled", false);
		config = new MySQLConfig(section);
		
		if (!enabled) return;
		connection = MySQLConnectionBuilder.createConnectionPool(config.getConnectionString());

		executeUpdate("CREATE TABLE IF NOT EXISTS mmoinv_players (uuid VARCHAR(36) NOT NULL,"
				+ "id INT(11) NOT NULL AUTO_INCREMENT,PRIMARY KEY (id),UNIQUE KEY (uuid));");
		executeUpdate("CREATE TABLE IF NOT EXISTS mmoinv_data (id INT(11) NOT NULL DEFAULT -1,"
				+ "slot_index INT(11) NOT NULL DEFAULT 0,stack JSON,UNIQUE KEY (id));");
	}

	public ResultSet getResult(final String sql) {
		try {
			CompletableFuture<QueryResult> future = connection.sendPreparedStatement(sql);
			return future.get().getRows();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void executeUpdate(final String sql) {
		try {
			connection.sendPreparedStatement(sql).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int getID(final String uuid) {
		executeUpdate("INSERT INTO mmoinv_players (uuid) SELECT * FROM (SELECT '" + uuid + "') AS tmp "
				+ "WHERE NOT EXISTS (SELECT uuid FROM mmoinv_players WHERE uuid = '" + uuid + "') LIMIT 1;");
		ResultSet result = getResult("SELECT id FROM mmoinv_players WHERE uuid = '" + uuid + "'");
		if (result.size() > 0)
			return result.get(0).getInt("id");
		return -1;
	}

	public SQLUserdata getUserData(final String uuid) {
		final SQLUserdata data = new SQLUserdata();
		final int id = getID(uuid);
		if(id == -1) {
			MMOInventory.plugin.getLogger().severe("Couldn't get ID for '" + uuid + "'");
			return data;
		}
		ResultSet result = getResult("SELECT slot_index,stack FROM mmoinv_data WHERE id = '" + id + "'");
		while(result.size() > 0) {
			ItemStack stack = gson.fromJson(result.get(0).getString("stack"), ItemStack.class);
			data.put(result.get(0).getInt("slot_index"), stack);
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
	
	public class MySQLConfig {
		private final String db, host, user, pass;
		private final int port;

		public MySQLConfig(ConfigurationSection config) {
			db = config.getString("database", "minecraft");
			host = config.getString("host", "localhost");
			port = config.getInt("port", 3306);
			user = config.getString("user", "mmolover");
			pass = config.getString("pass", "ILoveAria");
		}

		public String getConnectionString() {
			StringBuilder sb = new StringBuilder("jdbc:mysql://");
			sb.append(host).append(":").append(port).append("/").append(db)
			.append("?user=").append(user).append("&password=").append(pass);
			return sb.toString();
		}
	}
}
