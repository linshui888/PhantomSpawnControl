package com.github.alexqp.phantomspawncontrol.spawning.algorithm;

import com.github.alexqp.phantomspawncontrol.utility.SpawnCancelMsg;
import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpawnConditionsWorld implements SpawnConditionsHandler {

    @Nullable
    public static SpawnConditionsWorld build(final @NotNull JavaPlugin plugin, final @NotNull ConfigurationSection rootSection) {
        ConfigChecker configChecker = new ConfigChecker(plugin);

        ConfigurationSection section = configChecker.checkConfigSection(rootSection, "world", ConsoleErrorType.ERROR);
        if (section != null) {

            boolean mustBeNight = configChecker.checkBoolean(section, "must_be_night", ConsoleErrorType.WARN, true);
            double spawnProtectionRadius = configChecker.checkDouble(section, "spawn_protection_radius", ConsoleErrorType.WARN, 0);

            return new SpawnConditionsWorld(mustBeNight, spawnProtectionRadius);
        }

        return null;
    }

    private boolean mustBeNight;
    private double spawnProtectionRadiusSquared;

    private SpawnConditionsWorld(boolean mustBeNight, double spawnProtectionRadius) {
        this.mustBeNight = mustBeNight;
        this.spawnProtectionRadiusSquared = Math.pow(spawnProtectionRadius, 2);
    }

    private boolean isDay(World world) {
        return world.getEnvironment().equals(World.Environment.NORMAL) && world.getTime() <= 12541; // zombies will not burn
    }

    @Override
    public boolean shouldSpawn(final Player p, final JavaPlugin plugin) {
        World world = p.getWorld();

        if (mustBeNight && this.isDay(world)) {
            ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "NightTime"));
            return false;
        }

        if (world.getSpawnLocation().distanceSquared(p.getLocation()) <= spawnProtectionRadiusSquared) {
            ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "WorldSpawnProtectionRadius"));
            return false;
        }

        return true;
    }
}
