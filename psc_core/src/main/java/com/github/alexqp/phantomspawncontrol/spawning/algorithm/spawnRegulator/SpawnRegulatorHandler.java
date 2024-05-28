/*
 * Copyright (C) 2018-2024 Alexander Schmid
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
        ConsoleMessage.debug(this.getClass(), plugin, "Start computing PlayerSpawnRegulator for player " + ConsoleMessage.getPlayerString(p) + "...");
        for (PlayerSpawnRegulator regulator : playerSpawnRegulators) {
            if (!regulator.shouldSpawnAsync(p, plugin)) {
                ConsoleMessage.debug(this.getClass(), plugin, "PlayerSpawnRegulator " + regulator.getClass().getSimpleName() + " stopped Spawning for Player " + ConsoleMessage.getPlayerString(p));
                return false;
            }
        }
        ConsoleMessage.debug(this.getClass(), plugin, "Finished computing PlayerSpawnRegulator for player " + ConsoleMessage.getPlayerString(p) + ".");
        return true;
    }
}
