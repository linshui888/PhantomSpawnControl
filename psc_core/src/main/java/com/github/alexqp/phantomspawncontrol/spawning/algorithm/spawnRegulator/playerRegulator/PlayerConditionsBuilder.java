package com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.playerRegulator;

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.phantomspawncontrol.spawning.algorithm.SpawnAlgorithmAsync;
import com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.PlayerSpawnRegulator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

public class PlayerConditionsBuilder {

    public static LinkedHashSet<PlayerSpawnRegulator> build(final @NotNull JavaPlugin plugin, final @NotNull ConfigurationSection rootSection,
                                                            final @NotNull Objective scoreObjective,
                                                            final @NotNull SpawnAlgorithmAsync spawnAlgorithm) {
        LinkedHashSet<PlayerSpawnRegulator> regulators = new LinkedHashSet<>();

        ConfigChecker configChecker = new ConfigChecker(plugin);
        ConfigurationSection playerSection = configChecker.checkConfigSection(rootSection, "player", ConsoleErrorType.ERROR);
        if (playerSection != null) {
            regulators.add(GeneralPlayerConditions.build(plugin, playerSection));
            regulators.add(ScorePlayerConditions.build(plugin, playerSection, scoreObjective, spawnAlgorithm));
            regulators.add(EssentialsPlayerConditions.build(plugin, playerSection));
            regulators.add(BlockPlayerConditions.build(plugin, playerSection));
        }
        return regulators;
    }
}
