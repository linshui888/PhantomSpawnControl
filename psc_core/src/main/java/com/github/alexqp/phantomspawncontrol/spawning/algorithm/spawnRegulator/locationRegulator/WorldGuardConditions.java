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

package com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.locationRegulator;

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.LocationSpawnRegulator;
import com.github.alexqp.phantomspawncontrol.utility.SpawnCancelMsg;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldGuardConditions implements LocationSpawnRegulator {

    @Nullable
    public static WorldGuardConditions build(final @NotNull JavaPlugin plugin, final @NotNull ConfigurationSection rootSection) {
        ConfigChecker configChecker = new ConfigChecker(plugin);
        String sectionPath = "worldguard";

        ConfigurationSection section = configChecker.checkConfigSection(rootSection, sectionPath, ConsoleErrorType.ERROR);
        if (section != null) {
            boolean checkFlags = configChecker.checkBoolean(section, "check_flags", ConsoleErrorType.WARN, false);

            if (checkFlags) {
                try {
                    return new WorldGuardConditions(WorldGuard.getInstance());
                }
                catch (NoClassDefFoundError e) {
                    ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, rootSection.getCurrentPath(), sectionPath, "At least one WorldGuard feature was enabled while WorldGuard itself is not.");
                }
            }
        }
        return null;
    }

    private WorldGuard worldGuard;

    private WorldGuardConditions(@NotNull WorldGuard worldGuard) {
        this.worldGuard = worldGuard;
    }

    @Override // WorldGuard is thread safe!
    public boolean shouldSpawnAsync(@NotNull Location loc, @NotNull JavaPlugin plugin) {
        RegionContainer container = worldGuard.getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (!query.testState(BukkitAdapter.adapt(loc), null, Flags.MOB_SPAWNING)) {
            ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(loc, "WorldGuard MOB Flag"));
            return false;
        }
        return true;
    }
}
