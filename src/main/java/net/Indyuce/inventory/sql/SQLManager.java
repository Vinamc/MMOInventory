package net.Indyuce.inventory.sql;

import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;

import net.Indyuce.inventory.MMOInventory;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.util.SimpleWrapper;
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
		SimpleWrapper<Integer> id = new SimpleWrapper<>(-1);
		executeUpdate("INSERT INTO mmoinv_players (uuid) SELECT * FROM (SELECT '" + uuid + "') AS tmp "
				+ "WHERE NOT EXISTS (SELECT uuid FROM mmoinv_players WHERE uuid = '" + uuid + "') LIMIT 1;");
		getResult("SELECT id FROM mmoinv_players WHERE uuid = '" + uuid + "'", (result) -> {
			try {
				if (result.first())
					id.setValue(result.getInt("id"));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		return id.getValue();
	}

	public SQLUserdata getUserData(final String uuid) {
		final SQLUserdata data = new SQLUserdata();
		final int id = getID(uuid);
		if (id == -1) {
			MMOInventory.plugin.getLogger().severe("Couldn't get ID for '" + uuid + "'");
			return data;
		}
		try {
			getResultAsync("SELECT slot_index,stack FROM mmoinv_data WHERE id = '" + id + "'", (result) -> {
				try {
					if (result.first()) {
						ItemStack stack = MMOLib.plugin.getJson().parse(result.getString("stack"), ItemStack.class);
						data.put(result.getInt("slot_index"), stack);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}).get();
		} catch (InterruptedException | ExecutionException e) {
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
			builder.append("(" + id + ", " + entry.getKey() + ", '" + stack + "')").append(", ");
		}
		if (builder.length() > 2) {
			builder.setLength(builder.length() - 2); // gets rid of the last '", "'
			executeUpdate("INSERT INTO mmoinv_data (id,slot_index,stack) VALUES " + builder.toString() + ";");
		}
	}
}
