package com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

interface SpawnRegulator<T> { // should not be visible outside this package

    boolean shouldSpawnAsync(@NotNull final T obj, @NotNull final JavaPlugin plugin);
}
