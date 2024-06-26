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
import com.google.common.collect.Range;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;

public class GeneralLocationConditions implements LocationSpawnRegulator {

    @NotNull
    public static GeneralLocationConditions build(final @NotNull JavaPlugin plugin, final @NotNull ConfigurationSection rootSection) {
        ConfigChecker configChecker = new ConfigChecker(plugin);
        boolean checkAir = configChecker.checkBoolean(rootSection, "check_air_spawnpoint", ConsoleErrorType.WARN, true);
        int maxLightLevel = configChecker.checkInt(rootSection, "max_light_level", ConsoleErrorType.WARN, 7, Range.closed(0, 15));
        return new GeneralLocationConditions(checkAir, maxLightLevel);
    }

    private boolean checkAir;
    private int maxLightLevel;

    private GeneralLocationConditions(boolean checkAir, int maxLightLevel) {
        this.checkAir = checkAir;
        this.maxLightLevel = maxLightLevel;
    }

    @Override
    public boolean shouldSpawnAsync(@NotNull Location loc, @NotNull JavaPlugin plugin) {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        try {
            boolean checkAirResult = true;
            if (checkAir) {
                checkAirResult = scheduler.callSyncMethod(plugin, () -> {
                    if (!loc.getBlock().getType().equals(Material.AIR)) {
                        ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(loc, "not an air block."));
                        return false;
                    }
                    return true;
                }).get();
            }

            boolean checkLightResult = scheduler.callSyncMethod(plugin, () -> {
                if (loc.getBlock().getLightLevel() > maxLightLevel) {
                    ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(loc, "maxLightLevel"));
                    return false;
                }
                return true;
            }).get();
            return checkAirResult && checkLightResult;
        } catch (InterruptedException | ExecutionException e) {
            SpawnCancelMsg.printFutureGetError(plugin, this, null, e);
        }
        return false;
    }
}
