package com.github.alexqp.phantomspawncontrol.spawning.algorithm;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface SpawnConditionsHandler {

    boolean shouldSpawn(final Player p, final JavaPlugin plugin);
}
