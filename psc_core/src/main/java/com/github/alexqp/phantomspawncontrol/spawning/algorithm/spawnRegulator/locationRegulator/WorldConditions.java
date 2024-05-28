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
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;

public class WorldConditions implements LocationSpawnRegulator {

    @Nullable
    public static WorldConditions build(final @NotNull JavaPlugin plugin, final @NotNull ConfigurationSection rootSection) {
        ConfigChecker configChecker = new ConfigChecker(plugin);
        ConfigurationSection worldSection = configChecker.checkConfigSection(rootSection, "world", ConsoleErrorType.ERROR);
        if (worldSection != null) {
            boolean checkAir = configChecker.checkBoolean(worldSection, "must_be_night", ConsoleErrorType.WARN, true);
            double spawnProtectionRadius = configChecker.checkDouble(worldSection, "spawn_protection_radius", ConsoleErrorType.WARN, 0, Range.atLeast(0.0));

            HashSet<Integer> disabledMoonPhases = new HashSet<>();
            String moonPhasePath = "disabled_moon_phases";
            if (worldSection.contains(moonPhasePath)) {
                for (String phase : worldSection.getStringList(moonPhasePath)) {
                    try {
                        disabledMoonPhases.add(Integer.parseInt(phase));
                    }
                    catch (NumberFormatException e) {
                        configChecker.attemptConsoleMsg(ConsoleErrorType.WARN, worldSection, moonPhasePath, phase + " is no valid integer and was therefore skipped.");
                    }
                }
            }

            return new WorldConditions(checkAir, Math.pow(spawnProtectionRadius, 2), disabledMoonPhases);
        }
        return null;
    }

    private final boolean mustBeNight;
    private final double spawnProtectionRadiusSquared;
    private final HashSet<Integer> disabledMoonPhases;

    private WorldConditions(boolean mustBeNight, double spawnProtectionRadiusSquared, HashSet<Integer> disabledMoonPhases) {
        this.mustBeNight = mustBeNight;
        this.spawnProtectionRadiusSquared = spawnProtectionRadiusSquared;
        this.disabledMoonPhases = disabledMoonPhases;
    }

    @Override
    public boolean shouldSpawnAsync(@NotNull Location loc, @NotNull JavaPlugin plugin) {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        try {
            boolean mustBeNightResult = true;
            if (mustBeNight) {
                mustBeNightResult = scheduler.callSyncMethod(plugin, () -> {
                    World world = loc.getWorld();
                    if (world != null && world.getEnvironment().equals(World.Environment.NORMAL) && world.getTime() <= 12541) { // mobs burn
                        ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(loc, "not night time"));
                        return false;
                    }
                    return true;
                }).get();
            }

            boolean spawnProtectionRadiusResult = true;
            if (spawnProtectionRadiusSquared > 0) {
                spawnProtectionRadiusResult = scheduler.callSyncMethod(plugin, () -> {
                    World world = loc.getWorld();
                    if (world != null && world.getSpawnLocation().distanceSquared(loc) <= spawnProtectionRadiusSquared) {
                        ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(loc, "WorldSpawnProtection"));
                        return false;
                    }
                    return true;
                }).get();
            }

            boolean moonPhaseResult = true;
            if (!disabledMoonPhases.isEmpty()) {
                moonPhaseResult = scheduler.callSyncMethod(plugin, () -> {
                    World world = loc.getWorld();
                    if (world != null) {
                        Integer phase = (int) ((world.getFullTime() / 24000) % 8 + 1);
                        if (disabledMoonPhases.contains(phase)) {
                            ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(loc, "MoonPhase"));
                            return false;
                        }
                    }
                    return true;
                }).get();
            }

            return mustBeNightResult && spawnProtectionRadiusResult && moonPhaseResult;
        } catch (InterruptedException | ExecutionException e) {
            SpawnCancelMsg.printFutureGetError(plugin, this, null, e);
        }
        return false;
    }
}