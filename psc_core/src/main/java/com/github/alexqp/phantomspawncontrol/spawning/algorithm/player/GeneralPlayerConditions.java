package com.github.alexqp.phantomspawncontrol.spawning.algorithm.player;

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.events.PhantomPackSpawnEvent;
import com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.PlayerSpawnRegulator;
import com.github.alexqp.phantomspawncontrol.utility.ConfigReader;
import com.github.alexqp.phantomspawncontrol.utility.SpawnCancelMsg;
import com.google.common.collect.Range;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class GeneralPlayerConditions implements PlayerSpawnRegulator, Listener {

    @Nullable
    public static GeneralPlayerConditions build(final @NotNull JavaPlugin plugin, final @NotNull ConfigurationSection playerSection) {

        ConfigChecker configChecker = new ConfigChecker(plugin);

        boolean mustBeAboveSea = configChecker.checkBoolean(playerSection, "above_sea_level", ConsoleErrorType.WARN, true);
        double bedProtectionRadius = configChecker.checkDouble(playerSection, "bed_protection_radius", ConsoleErrorType.WARN, 100, Range.atLeast(0.0));

        Set<GameMode> spawnBlockGameModes = ConfigReader.getEnabledGameModes(configChecker, playerSection, "spawnblocking_gamemodes");

        ConfigurationSection section = configChecker.checkConfigSection(playerSection, "spawn_cooldown", ConsoleErrorType.ERROR);
        if (section != null) {

            int min = configChecker.checkInt(section, "min", ConsoleErrorType.WARN, 100, Range.atLeast(0));
            int max = configChecker.checkInt(section, "max", ConsoleErrorType.WARN, min + min/10, Range.atLeast(min));
            int[] spawnCooldown = {min, max};

            GeneralPlayerConditions instance = new GeneralPlayerConditions(plugin, mustBeAboveSea, bedProtectionRadius, spawnBlockGameModes, spawnCooldown);
            Bukkit.getPluginManager().registerEvents(instance, plugin);

            // easier checkable conditions should be added first. // TODO remove (and see below)
            /*instance.addSpawnConditionsHandler(SpawnConditionsPlayerScore.build(plugin, playerSection, scoreObjective, spawnAlgorithm));
            instance.addSpawnConditionsHandler(SpawnConditionsPlayerEssentials.build(plugin, playerSection));
            instance.addSpawnConditionsHandler(SpawnConditionsPlayerBlock.build(plugin, playerSection));*/

            return instance;
        }

        return null;
    }

    private JavaPlugin plugin;

    private boolean mustBeAboveSea;
    private double bedProtectionRadiusSquared;

    private Set<GameMode> spawnBlockGameModes;

    private int[] spawnCooldown; // {min, max}

    //private Set<SpawnConditionsHandler> spawnConditionsHandlers = new LinkedHashSet<>(); // keeps order

    private GeneralPlayerConditions(@NotNull JavaPlugin plugin, boolean mustBeAboveSea, double bedProtectionRadius, @NotNull Set<GameMode> spawnBlockGameModes, int[] spawnCooldown) {

        this.plugin = plugin;

        this.mustBeAboveSea = mustBeAboveSea;
        this.bedProtectionRadiusSquared = Math.pow(bedProtectionRadius, 2);
        this.spawnCooldown = spawnCooldown;
        this.spawnBlockGameModes = spawnBlockGameModes;
    }

    /*private void addSpawnConditionsHandler(@Nullable SpawnConditionsHandler handler) {
        if (handler == null) {
            ConsoleMessage.debug(this.getClass(), plugin, "Did not add SpawnConditionsHandler because it was null.");
            return;
        }
        this.spawnConditionsHandlers.add(handler);
        ConsoleMessage.debug(this.getClass(), plugin, "Added SpawnConditionsHandler " + handler.getClass().getSimpleName());
    }*/

    private int getRandomSpawnCooldown() {
        return ThreadLocalRandom.current().nextInt(spawnCooldown[0], spawnCooldown[1] + 1);
    }

    @Override
    public boolean shouldSpawnAsync(@NotNull Player p, @NotNull JavaPlugin plugin) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        try {
            boolean gameModeCheck = spawnBlockGameModes.isEmpty() || scheduler.callSyncMethod(plugin, () -> {
                if (spawnBlockGameModes.contains(p.getGameMode())) {
                    ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "GameMode"));
                    return false;
                }
                return true;
            }).get();

            boolean worldChecks = scheduler.callSyncMethod(plugin, () -> {
                World world = p.getWorld();

                if (mustBeAboveSea && world.getSeaLevel() > p.getLocation().getY()) {
                    ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "SeaLevel"));
                    return false;
                }

                Location bedSpawnLoc = p.getBedSpawnLocation();
                if (bedSpawnLoc != null && world.equals(bedSpawnLoc.getWorld()) && p.getLocation().distanceSquared(bedSpawnLoc) < bedProtectionRadiusSquared) {
                    ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "BedSpawnProtection"));
                    return false;
                }

                return true;
            }).get();

            return gameModeCheck && worldChecks;

        } catch (InterruptedException | ExecutionException e) {
            SpawnCancelMsg.printFutureGetError(plugin, this, e);
        }
        return false;
    }

    /*@Override
    public boolean shouldSpawn(final Player p, final JavaPlugin plugin) {

        if (spawnBlockGameModes.contains(p.getGameMode())) {
            ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "GameMode"));
            return false;
        }

        World world = p.getWorld();

        if (mustBeAboveSea && world.getSeaLevel() > p.getLocation().getY()) {
            ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "SeaLevel"));
            return false;
        }

        Location bedSpawnLoc = p.getBedSpawnLocation();
        if (bedSpawnLoc != null && world.equals(bedSpawnLoc.getWorld()) && p.getLocation().distanceSquared(bedSpawnLoc) < bedProtectionRadiusSquared) {
            ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "BedSpawnProtection"));
            return false;
        }

        for (SpawnConditionsHandler handler : spawnConditionsHandlers) {
            if (!handler.shouldSpawn(p, plugin))
                return false;
        }

        return true;
    }*/

    @EventHandler
    public void onPhantomPackSpawn(PhantomPackSpawnEvent e) {
        int current = e.getNextSpawnDelay();
        e.setNextSpawnDelay(Math.max(this.getRandomSpawnCooldown(), current));
        ConsoleMessage.debug(this.getClass(), plugin, "Next spawn delay may has been adjusted for player " + ConsoleMessage.getPlayerString(e.getTarget()) +
                " from " + current + " to " + e.getNextSpawnDelay() + " (SpawnCoolDown)");
    }
}
