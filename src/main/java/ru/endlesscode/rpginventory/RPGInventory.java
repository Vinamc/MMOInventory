package ru.endlesscode.rpginventory;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @deprecated illegal workaround
 */
@Deprecated
public class RPGInventory extends JavaPlugin {
    private static RPGInventory instance;

    public static RPGInventory getInstance() {
        if (instance == null)
            instance = new RPGInventory();
        return instance;
    }
}
