package com.github.alexqp.phantomspawncontrol.spawning;

import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.events.PhantomPackSpawnEvent;
import com.github.alexqp.phantomspawncontrol.spawning.algorithm.SpawnAlgorithmAsync;
import com.github.alexqp.phantomspawncontrol.utility.SpawnCancelMsg;
import com.github.alexqp.phantomspawncontrol.utility.WorldChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


public class SpawnRunnableAsync extends BukkitRunnable {

    private static JavaPlugin plugin;
    private static SpawnAlgorithmAsync spawnAlgorithm;
    private static Objective obj;
    private static WorldChecker worldChecker;

    private static ConcurrentHashMap<UUID, SpawnRunnableAsync> playerRunnables = new ConcurrentHashMap<>();

    public static void initiateSpawnRunnables(@NotNull JavaPlugin plugin,
                                              @NotNull SpawnAlgorithmAsync spawnAlgorithm, @NotNull Objective obj,
                                              @NotNull WorldChecker worldChecker) {
        SpawnRunnableAsync.plugin = plugin;
        SpawnRunnableAsync.spawnAlgorithm = spawnAlgorithm;
        SpawnRunnableAsync.obj = obj;
        SpawnRunnableAsync.worldChecker = worldChecker;
        SpawnRunnableAsync.playerRunnables = new ConcurrentHashMap<>();

        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(), plugin);
        ConsoleMessage.debug(SpawnRunnableAsync.class, plugin, "Registered PlayerConnectionListener regarding SpawnAlgorithm");
    }

    public static void startRunnables(@NotNull Collection<? extends Player> players) {
        for (Player p : players) {
            startRunnable(p);
        }
    }

    public static void startRunnable(@NotNull Player p) {
        startRunnable(p, spawnAlgorithm.getSpawnAttemptDelayAsync());
    }

    private static void startRunnable(@NotNull Player p, int delay) throws IllegalArgumentException {
        Objects.requireNonNull(plugin, "SpawnRunnables where not initiated (plugin == null)");
        Objects.requireNonNull(spawnAlgorithm, "SpawnRunnables where not initiated (spawnAlgorithm == null)");
        Objects.requireNonNull(obj, "SpawnRunnables where not initiated (obj == null)");
        Objects.requireNonNull(worldChecker, "SpawnRunnables where not initiated (worldChecker == null");

        if (delay <= 0)
            throw new IllegalArgumentException("delay must not be <= 0");

        UUID playerUUID = p.getUniqueId();

        if (playerRunnables.containsKey(playerUUID)) {
            ConsoleMessage.debug(SpawnRunnableAsync.class, plugin, "Tried to scheduled another task for player " + ConsoleMessage.getPlayerString(p));
            return;
        }

        SpawnRunnableAsync runnable = new SpawnRunnableAsync(playerUUID);
        playerRunnables.put(playerUUID, runnable);
        runnable.runTaskLaterAsynchronously(plugin, delay);
        ConsoleMessage.debug(SpawnRunnableAsync.class, plugin, "Next spawning attempts for player " + ConsoleMessage.getPlayerString(p) + " after delay = " + delay);
    }

    private static void startRunnableFromAsync(@NotNull UUID playerUUID) {
        SpawnRunnableAsync.startRunnableFromAsync(playerUUID, spawnAlgorithm.getSpawnAttemptDelayAsync());
    }

    private static void startRunnableFromAsync(@NotNull UUID playerUUID, int delay) throws IllegalArgumentException {
            Bukkit.getScheduler().runTask(plugin, () -> {
            Player p = Bukkit.getPlayer(playerUUID);
            BukkitRunnable runnable = playerRunnables.remove(playerUUID);
            if (runnable != null) {
                runnable.cancel();
            }

            if (p != null) {
                SpawnRunnableAsync.startRunnable(p, delay);
            } else {
                ConsoleMessage.debug(SpawnRunnableAsync.class, plugin, "While restarting the process player became null " + playerUUID);
            }
        });
    }

    public static void stopRunnable(@NotNull Player p) {
        UUID playerUUID = p.getUniqueId();

        SpawnRunnableAsync runnable = playerRunnables.get(playerUUID);
        if (runnable != null) {
            runnable.cancel();
        }

        playerRunnables.remove(playerUUID);
        ConsoleMessage.debug(SpawnRunnableAsync.class, plugin, "Canceled spawning attempts for player " + ConsoleMessage.getPlayerString(p));
    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    private final UUID playerUUID;

    private SpawnRunnableAsync(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    private void sendStopDebug() {
        ConsoleMessage.debug(SpawnRunnableAsync.class, plugin, "Completely stopped runnable because player was null for " + playerUUID);
    }

    private void spawnPhantomsAndRestartAsync(@NotNull Map<Location, Consumer<Phantom>> phantoms) {
        new BukkitRunnable() {

            @Override
            public void run() {
                Player p = Bukkit.getPlayer(playerUUID);
                if (p == null) {
                    ConsoleMessage.debug(this.getClass(), plugin, "Could not proceed with spawning because player is null while spawning. Stopping runnable...");
                    sendStopDebug();
                    return;
                }

                World world = p.getWorld();
                HashSet<Phantom> phantomPack = new HashSet<>();

                for (Location loc : phantoms.keySet()) {
                    ConsoleMessage.debug(this.getClass(), plugin, "Spawning phantom for player " + ConsoleMessage.getPlayerString(p) + "...");
                    Phantom phantom = world.spawn(loc, Phantom.class, phantoms.get(loc));
                    phantom.setTarget(p);
                    phantomPack.add(phantom);
                }

                int delay = spawnAlgorithm.getSpawnAttemptDelayAsync();

                if (phantomPack.size() > 0) {
                    PhantomPackSpawnEvent packSpawnEvent = new PhantomPackSpawnEvent(phantomPack, p, delay);
                    Bukkit.getPluginManager().callEvent(packSpawnEvent);
                    delay = packSpawnEvent.getNextSpawnDelay();
                }

                if (delay <= 0) {
                    ConsoleMessage.debug(this.getClass(), plugin, "Could not reschedule spawning attempts because sth messed up the delay = " + delay);
                    return;
                }

                playerRunnables.remove(p.getUniqueId());
                SpawnRunnableAsync.startRunnable(p, delay);
            }
        }.runTask(plugin);
    }

    @Override
    public void run() {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        try {
            Player p = scheduler.callSyncMethod(plugin, () -> Bukkit.getPlayer(playerUUID)).get();
            if (p == null) {
                this.sendStopDebug();
            } else if (worldChecker.isWorldEnabled(p.getWorld())) {
                if (spawnAlgorithm.shouldSpawnAsync(p, plugin)) {
                    int score = scheduler.callSyncMethod(plugin, () -> obj.getScore(p.getName()).getScore()).get();
                    HashMap<Location, Consumer<Phantom>> phantoms = new HashMap<>();
                    for (Location loc : spawnAlgorithm.getSpawnLocationsAsync(p, plugin)) {
                        phantoms.put(loc, spawnAlgorithm.getPhantomStatsConsumerAsync(score));
                    }
                    this.spawnPhantomsAndRestartAsync(phantoms);
                    return;
                }
            } else {
                ConsoleMessage.debug(this.getClass(), plugin, "world of player " + ConsoleMessage.getPlayerString(p) + " is not enabled.");
            }
            SpawnRunnableAsync.startRunnableFromAsync(playerUUID);
        }
        catch (InterruptedException | ExecutionException e) {
            SpawnCancelMsg.printFutureGetError(plugin, this, e);
        }
    }
}
