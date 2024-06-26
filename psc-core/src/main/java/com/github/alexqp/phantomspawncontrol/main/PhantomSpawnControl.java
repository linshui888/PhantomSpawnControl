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

package com.github.alexqp.phantomspawncontrol.main;

import com.github.alexqp.commons.bstats.bukkit.Metrics;
import com.github.alexqp.commons.bstats.charts.SimplePie;
import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.commons.messages.Debugable;
import com.github.alexqp.phantomspawncontrol.command.PhantomCommand;
import com.github.alexqp.phantomspawncontrol.data.Saveable;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStats;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStatsContainer;
import com.github.alexqp.phantomspawncontrol.data.phantom.loottables.AbstractPhantomLootTable;
import com.github.alexqp.phantomspawncontrol.data.player.PlayerConnectionListener;
import com.github.alexqp.phantomspawncontrol.data.player.PlayerStatsContainer;
import com.github.alexqp.phantomspawncontrol.spawning.DisableNaturalPhantomSpawning;
import com.github.alexqp.phantomspawncontrol.spawning.SpawnRunnableAsync;
import com.github.alexqp.phantomspawncontrol.spawning.algorithm.SpawnAlgorithmAsync;
import com.github.alexqp.phantomspawncontrol.utility.WorldChecker;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;


public class PhantomSpawnControl extends JavaPlugin implements Debugable {

    /*
     * Changelog v4.1.0+:
     * Fixed: default config was not reloaded by command.
     * // TODO
     *     - if players sleep under direct sky access, phantoms have a chance to spawn based on their score * some multiplier (they get kicked out of the bed)
     *              -> make another class (BedListener) to hook into SpawnAlgorithm#getSpawnLocations <- without SpawnConditions-Checking!
     *     - soft-depend TimeControl to determine day-worlds/time
     *     - ESSENTIALS: /rest command support?
     */

    private static final String defaultInternalsVersion = "Internals_v1_20_6";
    private static final String scoreboardObjectiveName = "PLUGIN_PSC";

    private static InternalsProvider internals;
    static {
        try {
            String packageName = PhantomSpawnControl.class.getPackage().getName();
            Bukkit.getConsoleSender().sendMessage("BUKKIT VERSION = " + Bukkit.getServer().getBukkitVersion());
            String minecraftVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
            String internalsName = getInternalsName(minecraftVersion);
            if (defaultInternalsVersion.equals(internalsName)) {
                Bukkit.getLogger().log(Level.INFO, PhantomSpawnControl.class.getSimpleName() + " is using the latest implementation (last tested for " + defaultInternalsVersion + ").");
                internals = new InternalsProvider();
            } else {
                Bukkit.getLogger().log(Level.INFO, PhantomSpawnControl.class.getSimpleName() + " is using the implementation for version " + internalsName + ".");
                internals = (InternalsProvider) Class.forName(packageName + "." + internalsName).getDeclaredConstructor().newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException exception) {
            Bukkit.getLogger().log(Level.WARNING, PhantomSpawnControl.class.getSimpleName() + " could not find an updated implementation for this server version. " +
                    "However the plugin is trying to use the latest implementation which should work if Minecraft did not change drastically (last tested version: " + defaultInternalsVersion + ").");
            internals = new InternalsProvider();
        }
    }

    private static String getInternalsName(String minecraftVersion) {
        if (minecraftVersion.startsWith("1.13") || minecraftVersion.startsWith("1.14") || minecraftVersion.startsWith("1.15") || minecraftVersion.startsWith("1.16") || minecraftVersion.startsWith("1.17") || minecraftVersion.startsWith("1.18")
                || minecraftVersion.equals("1.19") || minecraftVersion.equals("1.19.1") || minecraftVersion.equals("1.19.2")) {
            return "Internals_v1_19_2";
        } else if (minecraftVersion.startsWith("1.19") || minecraftVersion.equals("1.20") || minecraftVersion.startsWith("1.20.1")) {
            // needed to add InternalsProvider#addPluginScoreboardObjective after v1_19_R1
            return "Internals_v1_20_1";
        }
        // needed to add InternalsProvider#spawnPhantom after v1_20_R1 (bukkit consumer changed to java consumer)
        return defaultInternalsVersion;
    }

    static {
        ConfigurationSerialization.registerClass(PhantomStats.class, "PhantomStats");
        ConfigurationSerialization.registerClass(AbstractPhantomLootTable.class, "AbstractPhantomLootTable");
    }

    private boolean debug = false;
    private PhantomStatsContainer phantomStatsContainer;

    private final Set<Saveable> saveOnDisable = new HashSet<>();

    @Override
    public void onEnable() {
        Metrics bstats = new Metrics(this, 3018);
        this.getLogger().info("This plugin was made by alex_qp.");

        this.onRealEnable();

        bstats.addCustomChart(new SimplePie("giant_phantoms", () -> {
            if (phantomStatsContainer == null)
                return "0";
            else
                return String.valueOf(phantomStatsContainer.getDefinedScores().size());
        }));

        this.updateChecker();
    }

    @Override
    public void onDisable() {
        ConsoleMessage.debug((Debugable) this, "Initiating saving of data (saveables = " + saveOnDisable.size() + ")");
        for (Saveable saveable : saveOnDisable) {
            saveable.save();
        }
    }

    public void onReload() {
        ConsoleMessage.debug((Debugable) this, "Initiate reloading...");

        Bukkit.getScheduler().cancelTasks(this);
        ConsoleMessage.debug((Debugable) this, "Cancelled all tasks");

        HandlerList.unregisterAll(this);
        ConsoleMessage.debug((Debugable) this, "Unregistered all listeners");

        for (Saveable saveable : saveOnDisable) {
            saveable.save();
        }
        saveOnDisable.clear();
        ConsoleMessage.debug((Debugable) this, "Saved all data and cleared saveOnDisable");
        ConsoleMessage.debug((Debugable) this, "Re-Enabling the plugin...");

        this.onRealEnable();
    }

    private void onRealEnable() {
        this.saveDefaultConfig();
        this.reloadConfig();
        this.checkDebugMode();

        ConfigChecker configChecker = new ConfigChecker(this);

        ScoreboardManager scoreboardManager = Bukkit.getServer().getScoreboardManager();
        if (scoreboardManager == null) {
            ConsoleMessage.send(ConsoleErrorType.ERROR, this, "No world was loaded yet, delaying enabling...");
            new BukkitRunnable() {

                @Override
                public void run() {
                    onRealEnable();
                }
            }.runTaskLater(this, 100);
            return;
        }

        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
        Objective scoreObjective = scoreboard.getObjective(scoreboardObjectiveName);
        if (scoreObjective == null) {
            scoreObjective = internals.addPluginScoreboardObjective(scoreboard, scoreboardObjectiveName);
            ConsoleMessage.send(ConsoleErrorType.WARN, this, "There was no objective " + scoreboardObjectiveName + " yet so it has been added.");
        }

        new DisableNaturalPhantomSpawning(this);

        ConfigurationSection section = configChecker.checkConfigSection(this.getConfig(), "phantom_spawn", ConsoleErrorType.ERROR);
        if (section != null) {

            boolean enableSpawning = configChecker.checkBoolean(section, "enable", ConsoleErrorType.WARN, true);

            if (configChecker.checkBoolean(section, "giant_phantoms", ConsoleErrorType.WARN, true)) {
                 phantomStatsContainer = new PhantomStatsContainer(this, internals);
                 phantomStatsContainer.load(ConsoleErrorType.WARN);
                 saveOnDisable.add(phantomStatsContainer);
            }

            boolean playerStatsPreventTargeting = configChecker.checkBoolean(this.getConfig(), "playerdata_prevent_targeting", ConsoleErrorType.WARN, false);

            PlayerStatsContainer playerStatsContainer = new PlayerStatsContainer(this, playerStatsPreventTargeting);
            if (configChecker.checkBoolean(this.getConfig(), "save_playerdata", ConsoleErrorType.WARN, true)) {
                playerStatsContainer.load(ConsoleErrorType.NONE);
                saveOnDisable.add(playerStatsContainer);
                Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(playerStatsContainer), this);
                ConsoleMessage.debug((Debugable) this, "Data will be saved and loaded for " + playerStatsContainer.getClass().getSimpleName());
            }

            if (enableSpawning) {

                ConfigurationSection worldSection = configChecker.checkConfigSection(section, "world_configuration", ConsoleErrorType.ERROR);
                if (worldSection != null) {

                    WorldChecker worldChecker = new WorldChecker(WorldChecker.getEnabledEnvsBySection(configChecker.checkConfigSection(worldSection, "enabled_environments", ConsoleErrorType.ERROR)),
                            worldSection.getStringList("disabled_worlds"));

                    section = configChecker.checkConfigSection(section, "spawning_conditions", ConsoleErrorType.ERROR);
                    if (section != null) {

                        SpawnAlgorithmAsync spawnAlgorithm = SpawnAlgorithmAsync.build(this, section, scoreObjective, phantomStatsContainer, playerStatsContainer);
                        if (spawnAlgorithm != null) {

                            SpawnRunnableAsync.initiateSpawnRunnables(this, internals, spawnAlgorithm, scoreObjective, worldChecker);
                            ConsoleMessage.debug((Debugable) this, "Initiated SpawnRunnables.");

                            SpawnRunnableAsync.startRunnables(Bukkit.getServer().getOnlinePlayers());
                        }
                    }
                }
            }

            PhantomCommand command = new PhantomCommand(this, internals, playerStatsContainer, phantomStatsContainer);
            command.register();
        }

    }

    private void checkDebugMode() {
        String configName = "debug";
        if (this.getConfig().contains(configName) && this.getConfig().isBoolean(configName) && this.getConfig().getBoolean(configName)) {
            debug = true;
            ConsoleMessage.debug((Debugable) this, "DebugMode enabled.");
        }
    }

    @Override
    public boolean getDebug() {
        return debug;
    }

    private void updateChecker() {
        int spigotResourceID = 71538;
        ConfigChecker configChecker = new ConfigChecker(this);
        ConfigurationSection updateCheckerSection = configChecker.checkConfigSection(this.getConfig(), "updatechecker", ConsoleErrorType.ERROR);
        if (updateCheckerSection != null && configChecker.checkBoolean(updateCheckerSection, "enable", ConsoleErrorType.WARN, true)) {
            ConsoleMessage.debug((Debugable) this, "enabled UpdateChecker");

            new UpdateChecker(this, UpdateCheckSource.SPIGOT, String.valueOf(spigotResourceID))
                    .setDownloadLink(spigotResourceID)
                    .setChangelogLink("https://www.spigotmc.org/resources/" + spigotResourceID + "/updates")
                    .setDonationLink("https://paypal.me/alexqpplugins")
                    .setNotifyOpsOnJoin(configChecker.checkBoolean(updateCheckerSection, "notify_op_on_login", ConsoleErrorType.WARN, true))
                    .setNotifyByPermissionOnJoin("phantomspawncontrol.updatechecker")
                    .checkEveryXHours(24).checkNow();
        }
    }
}