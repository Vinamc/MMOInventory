package net.Indyuce.inventory.compat.list;

import net.Indyuce.inventory.compat.LevelModule;
import net.Indyuce.inventory.inventory.InventoryHandler;

public class DefaultHook implements LevelModule {

    @Override
    public int getLevel(InventoryHandler player) {
        return player.getPlayer().getLevel();
    }
}