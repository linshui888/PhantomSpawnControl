package com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator;

import com.github.alexqp.commons.messages.ConsoleMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class SpawnRegulatorHandler {

    private LinkedHashSet<LocationSpawnRegulator> locationSpawnRegulators = new LinkedHashSet<>();
    private LinkedHashSet<PlayerSpawnRegulator> playerSpawnRegulators = new LinkedHashSet<>();

    public SpawnRegulatorHandler() {}

    public SpawnRegulatorHandler addSpawnRegulator(@NotNull final JavaPlugin plugin, @Nullable final LocationSpawnRegulator regulator) {
        if (regulator != null) {
            locationSpawnRegulators.add(regulator);
            ConsoleMessage.debug(this.getClass(), plugin, "Added LocationSpawnRegulator " + regulator.getClass().getSimpleName());
        } else {
            ConsoleMessage.debug(this.getClass(), plugin, "Did not add LocationSpawnRegulator because it was null");
        }
        return this;
    }

    public SpawnRegulatorHandler addSpawnRegulator(@NotNull final JavaPlugin plugin, @Nullable final PlayerSpawnRegulator regulator) {
        if (regulator != null) {
            playerSpawnRegulators.add(regulator);
            ConsoleMessage.debug(this.getClass(), plugin, "Added PlayerSpawnRegulator " + regulator.getClass().getSimpleName());
        } else {
            ConsoleMessage.debug(this.getClass(), plugin, "Did not add PlayerSpawnRegulator because it was null");
        }
        return this;
    }

    public Set<Location> computeSpawnRegulatorAsync(@NotNull final Set<Location> locations, @NotNull final JavaPlugin plugin) {
        ConsoleMessage.debug(this.getClass(), plugin, "Start computing LocationSpawnRegulator with " + locations.size() + " locations...");
        HashSet<Location> set = new HashSet<>(locations);
        for (Location loc : locations) {
            for (LocationSpawnRegulator regulator : locationSpawnRegulators) {
                if (!regulator.shouldSpawnAsync(loc, plugin)) {
                    set.remove(loc);
                }
            }
        }
        ConsoleMessage.debug(this.getClass(), plugin, "Finished computing LocationSpawnRegulator (" + set.size() + "/" + locations.size() + ")");
        return set;
    }

    public boolean computeSpawnRegulatorAsync(@NotNull Player p, @NotNull final JavaPlugin plugin) {
        ConsoleMessage.debug(this.getClass(), plugin, "Start computing PlayerSpawnRegulator for Player " + ConsoleMessage.getPlayerString(p) + "...");
        for (PlayerSpawnRegulator regulator : playerSpawnRegulators) {
            if (!regulator.shouldSpawnAsync(p, plugin)) {
                ConsoleMessage.debug(this.getClass(), plugin, "PlayerSpawnRegulator " + regulator.getClass().getSimpleName() + " stopped Spawning for Player " + ConsoleMessage.getPlayerString(p));
                return false;
            }
        }
        return true;
    }
}
