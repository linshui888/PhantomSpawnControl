package com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.playerRegulator;

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.spawning.algorithm.SpawnAlgorithmAsync;
import com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.PlayerSpawnRegulator;
import com.github.alexqp.phantomspawncontrol.utility.ConfigReader;
import com.github.alexqp.phantomspawncontrol.utility.SpawnCancelMsg;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ScorePlayerConditions implements PlayerSpawnRegulator, Listener {

    private static final String stopResetPermission = "phantomspawncontrol.stopreset";

    @Nullable
    static ScorePlayerConditions build(final @NotNull JavaPlugin plugin, final @NotNull ConfigurationSection playerSection,
                                       final @NotNull Objective scoreObjective,
                                       final @NotNull SpawnAlgorithmAsync spawnAlgorithm) {

        ConfigChecker configChecker = new ConfigChecker(plugin);

        ConfigurationSection section = configChecker.checkConfigSection(playerSection, "score", ConsoleErrorType.ERROR);
        if (section != null) {

            int minScore = configChecker.checkInt(section, "min_score", ConsoleErrorType.WARN, 72000);
            Set<GameMode> resetGameModes = ConfigReader.getEnabledGameModes(configChecker, section, "reset_gamemodes");

            ScorePlayerConditions instance = new ScorePlayerConditions(plugin, scoreObjective, minScore, resetGameModes, spawnAlgorithm);
            Bukkit.getPluginManager().registerEvents(instance, plugin);
            return instance;
        }

        return null;
    }

    private JavaPlugin plugin;

    private Objective scoreObjective;
    private int minScore;
    private Set<GameMode> resetGameModes;

    private ScorePlayerConditions(@NotNull JavaPlugin plugin, @NotNull Objective scoreObjective,
                                  int minScore, @NotNull Set<GameMode> resetGameModes, @NotNull SpawnAlgorithmAsync spawnAlgorithm) {
        this.plugin = plugin;
        this.scoreObjective = scoreObjective;
        this.minScore = minScore;

        spawnAlgorithm.setRandomChanceScore(minScore);

        this.resetGameModes = resetGameModes;
    }

    @Override
    public boolean shouldSpawnAsync(@NotNull Player p, @NotNull JavaPlugin plugin) {
        try {
            return Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                if (minScore > scoreObjective.getScore(p.getName()).getScore()) {
                    ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "Score"));
                    return false;
                }
                return true;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            SpawnCancelMsg.printFutureGetError(plugin, this, e);
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        Player p = e.getPlayer();
        if (resetGameModes.contains(e.getNewGameMode()) && !p.hasPermission(stopResetPermission)) {
            scoreObjective.getScore(p.getName()).setScore(0);
            ConsoleMessage.debug(this.getClass(), plugin, "Reset Score of player " + p.getName() + " because he switched to GameMode " + e.getNewGameMode().name());
        }
    }
}