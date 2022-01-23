package net.Indyuce.inventory.compat;

import net.Indyuce.inventory.compat.list.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.inject.Provider;

public enum ModuleType {
    AURELIUMSKILLS("AureliumSkills", AureliumSkillsHook::new),
    BATTLELEVELS("BattleLevels", BattleLevelsHook::new),
    HEROES("Heroes", HeroesHook::new),
    MCMMO("mcMMO", McMMOHook::new),
    MCRPG("McRPG", McRPGHook::new),
    MMOCORE("MMOCore", MMOCoreHook::new),
    PROSKILLAPI("ProSkillAPI", SkillAPIHook::new),
    RACESANDCLASSES("RacesAndClasses", RacesAndClassesHook::new),
    RPGPLAYERLEVELING("RPGPlayerLeveling", RPGPlayerLevelingHook::new),
    SKILLAPI("SkillAPI", SkillAPIHook::new),
    SKILLS("Skills", SkillsHook::new),
    SKILLSPRO("SkillsPro", SkillsProHook::new),
    ;

    private final Provider<Object> moduleProvider;
    private final String pluginName;

    ModuleType(String pluginName, Provider<Object> moduleProvider) {
        this.pluginName = pluginName;
        this.moduleProvider = moduleProvider;
    }

    public Object getModule() {
        return moduleProvider.get();
    }

    public Plugin getPluginName() {
        return Bukkit.getPluginManager().getPlugin(pluginName);
    }
}
