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

import java.util.concurrent.ExecutionException;

public class WorldConditions implements LocationSpawnRegulator {

    @Nullable
    public static WorldConditions build(final @NotNull JavaPlugin plugin, final @NotNull ConfigurationSection rootSection) {
        ConfigChecker configChecker = new ConfigChecker(plugin);
        ConfigurationSection worldSection = configChecker.checkConfigSection(rootSection, "world", ConsoleErrorType.ERROR);
        if (worldSection != null) {
            boolean checkAir = configChecker.checkBoolean(rootSection, "must_be_night", ConsoleErrorType.WARN, true);
            double spawnProtectionRadius = configChecker.checkDouble(rootSection, "spawn_protection_radius", ConsoleErrorType.WARN, 0, Range.atLeast(0.0));
            return new WorldConditions(checkAir, Math.pow(spawnProtectionRadius, 2));
        }
        return null;
    }

    boolean mustBeNight;
    double spawnProtectionRadiusSquared;

    private WorldConditions(boolean mustBeNight, double spawnProtectionRadiusSquared) {
        this.mustBeNight = mustBeNight;
        this.spawnProtectionRadiusSquared = spawnProtectionRadiusSquared;
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

            return mustBeNightResult && spawnProtectionRadiusResult;
        } catch (InterruptedException | ExecutionException e) {
            SpawnCancelMsg.printFutureGetError(plugin, this, null, e);
        }
        return false;
    }
}