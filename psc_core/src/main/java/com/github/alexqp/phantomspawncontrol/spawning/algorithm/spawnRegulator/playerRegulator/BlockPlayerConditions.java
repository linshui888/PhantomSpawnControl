package com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.playerRegulator;

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.PlayerSpawnRegulator;
import com.github.alexqp.phantomspawncontrol.utility.SpawnCancelMsg;
import com.github.alexqp.phantomspawncontrol.utility.WorldChecker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class BlockPlayerConditions implements PlayerSpawnRegulator {

    @Nullable
    static BlockPlayerConditions build(final @NotNull JavaPlugin plugin, final @NotNull ConfigurationSection playerSection) {

        ConfigChecker configChecker = new ConfigChecker(plugin);

        ConfigurationSection section = configChecker.checkConfigSection(playerSection, "blocks", ConsoleErrorType.ERROR);
        if (section != null) {

            boolean noSolidAbove = configChecker.checkBoolean(section, "no_solid_above", ConsoleErrorType.WARN, true);

            Set<Material> ignoredSolidBlocks = new HashSet<>();
            if (noSolidAbove) {
                ignoredSolidBlocks = getMaterialSet(configChecker, section, "ignored_solid");
            }

            Set<Material> addBlocks = getMaterialSet(configChecker, section, "add_blocks");

            section = configChecker.checkConfigSection(section, "worlds", ConsoleErrorType.ERROR);
            if (section != null) {

                WorldChecker worldChecker = new WorldChecker(WorldChecker.getEnabledEnvsBySection(configChecker.checkConfigSection(section, "enabled_environments", ConsoleErrorType.ERROR)), section.getStringList("disabled_worlds"));

                return new BlockPlayerConditions(noSolidAbove, ignoredSolidBlocks, addBlocks, worldChecker);
            }

        }

        return null;
    }

    private static @NotNull Set<Material> getMaterialSet(ConfigChecker configChecker, ConfigurationSection section, String path) {
        Set<Material> matSet = new HashSet<>();
        if (!section.contains(path)) {
            return matSet;
        }

        for (String matName : section.getStringList(path)) {

            Material mat = Material.matchMaterial(matName);
            if (mat == null || matName.contains("LEGACY")) {
                configChecker.attemptConsoleMsg(ConsoleErrorType.WARN, section.getCurrentPath() + "." + path, matName, "material is not valid or legacy, skipped.");
                continue;
            }

            matSet.add(mat);
        }

        return matSet;
    }

    private boolean noSolidAbove;
    private HashSet<Material> ignoredSolidBlocks;
    private HashSet<Material> addedBlocks;
    private WorldChecker worldChecker;

    private BlockPlayerConditions(boolean noSolidAbove, Collection<? extends Material> ignoredSolidBlocks, @NotNull Collection<? extends Material> addBlocks, @NotNull WorldChecker worldChecker) {
        this.noSolidAbove = noSolidAbove;
        this.ignoredSolidBlocks = new HashSet<>(ignoredSolidBlocks);
        this.addedBlocks = new HashSet<>(addBlocks);
        this.worldChecker = worldChecker;

        if (noSolidAbove)
            Objects.requireNonNull(ignoredSolidBlocks);
    }

    @Override
    public boolean shouldSpawnAsync(@NotNull Player p, @NotNull JavaPlugin plugin) {
        try {
            return Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                World world = p.getWorld();
                if (!worldChecker.isWorldEnabled(world)) {
                    ConsoleMessage.debug(this.getClass(), plugin, "Skipped block check for player " + ConsoleMessage.getPlayerString(p) + " because world " + world.getName() + " was not enabled.");
                    return true;
                }

                if (!noSolidAbove && addedBlocks.isEmpty()) {
                    ConsoleMessage.debug(this.getClass(), plugin, "Skipped block check for player " + ConsoleMessage.getPlayerString(p) + " because noSolidAbove == false and no added blocks.");
                    return true;
                }

                int x = p.getLocation().getBlockX();
                int z = p.getLocation().getBlockZ();

                int highestY = world.getHighestBlockYAt(p.getLocation());

                for (int y = p.getLocation().getBlockY() + 1; y <= highestY; y++) {
                    Material blockType = world.getBlockAt(x, y, z).getType();

                    if (noSolidAbove && blockType.isSolid() && !ignoredSolidBlocks.contains(blockType)) {
                        ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "solid block at y = " + y));
                        return false;

                    } else if (addedBlocks.contains(blockType)) {
                        ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "added block at y = " + y));
                        return false;
                    }
                }
                return true;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            SpawnCancelMsg.printFutureGetError(plugin, this, e);
        }
        return false;
    }
}
