package net.Indyuce.inventory.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;

import net.Indyuce.inventory.MMOInventory;
import net.mmogroup.mmolib.sql.MMODataSource;

public class SQLManager extends MMODataSource {
	private final Gson gson = new Gson();

	@Override
	public void load() {
		executeUpdateAsync("CREATE TABLE IF NOT EXISTS mmoinv_players (uuid VARCHAR(36) NOT NULL,"
				+ "id INT(11) NOT NULL AUTO_INCREMENT,PRIMARY KEY (id),UNIQUE KEY (uuid));");
		executeUpdateAsync("CREATE TABLE IF NOT EXISTS mmoinv_data (id INT(11) NOT NULL DEFAULT -1,"
				+ "slot_index INT(11) NOT NULL DEFAULT 0,stack JSON,UNIQUE KEY (id));");
	}

	public int getID(final String uuid) {
		try {
			executeUpdateAsync("INSERT INTO mmoinv_players (uuid) SELECT * FROM (SELECT '" + uuid + "') AS tmp "
					+ "WHERE NOT EXISTS (SELECT uuid FROM mmoinv_players WHERE uuid = '" + uuid + "') LIMIT 1;").get();
			ResultSet result = getResultAsync("SELECT id FROM mmoinv_players WHERE uuid = '" + uuid + "'").get();
			if (result.first())
				return result.getInt("id");
		} catch (InterruptedException | ExecutionException | SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public SQLUserdata getUserData(final String uuid) {
		final SQLUserdata data = new SQLUserdata();
		final int id = getID(uuid);
		if (id == -1) {
			MMOInventory.plugin.getLogger().severe("Couldn't get ID for '" + uuid + "'");
			return data;
		}
		try {
			ResultSet result = getResultAsync("SELECT slot_index,stack FROM mmoinv_data WHERE id = '" + id + "'").get();
			if (result.first()) {
				ItemStack stack = gson.fromJson(result.getString("stack"), ItemStack.class);
				data.put(result.getInt("slot_index"), stack);
			}
		} catch (InterruptedException | ExecutionException | SQLException e) {
			e.printStackTrace();
		}

		return data;
	}

	public void save(final String uuid, final Set<Entry<Integer, ItemStack>> inventory) {
		try {
			final int id = getID(uuid);
			executeUpdateAsync("DELETE FROM mmoinv_data WHERE id = '" + id + "'").get();
			StringBuilder builder = new StringBuilder();
			for (Entry<Integer, ItemStack> entry : inventory) {
				String stack = gson.toJson(entry.getValue());
				builder.append("(" + id + ", " + entry.getKey() + ", '" + stack + "')");
				builder.append(", ");
			}
			builder.setLength(builder.length() - 2); // gets rid of the last '", "'
			executeUpdateAsync("INSERT INTO mmoinv_data (id,slot_index,stack) VALUES " + builder.toString() + ";")
					.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
