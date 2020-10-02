package com.github.alexqp.phantomspawncontrol.data.player;

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.dataHandler.DataHandler;
import com.github.alexqp.commons.dataHandler.LoadSaveException;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.data.Saveable;
import com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.PlayerSpawnRegulator;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStatsContainer implements Listener, PlayerSpawnRegulator, Saveable {

    private JavaPlugin plugin;
    private DataHandler dataHandler;

    private ConcurrentHashMap<UUID, PlayerStats> allStats = new ConcurrentHashMap<>();

    public PlayerStatsContainer(JavaPlugin plugin, boolean preventTargeting) {
        try {
            this.plugin = plugin;
            dataHandler = new DataHandler(plugin, "playerdata");
        } catch (LoadSaveException e) {
            ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, e.getMessage());
        }

        if (preventTargeting) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            ConsoleMessage.debug(this.getClass(), plugin, "Target-Prevention was activated.");
        }
    }

    public void load(ConsoleErrorType errorType) {
        ConsoleMessage.debug(this.getClass(), plugin, "Loading PlayerStats...");
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.load(errorType, p);
        }
        ConsoleMessage.debug(this.getClass(), plugin, "Completed loading of PlayerStats.");
    }

    void load(ConsoleErrorType errorType, UUID uuid) {
        Objects.requireNonNull(dataHandler, "dataHandler must not be null for load method.");

        YamlConfiguration ymlFile = dataHandler.loadYmlFile(uuid.toString());
        ConfigChecker configChecker = new ConfigChecker(plugin, ymlFile);

        allStats.put(uuid, new PlayerStats(configChecker.checkBoolean(ymlFile, PlayerStats.configNames[0], errorType, true)));

        ConsoleMessage.debug(this.getClass(), plugin, "Loaded data for UUID " + uuid.toString());
    }

    public void load(@NotNull ConsoleErrorType errorType, @NotNull Player p) {
        if (!p.hasPermission("phantomspawncontrol.defaultplayerdata")) {
            this.load(errorType, p.getUniqueId());
            return;
        }
        allStats.put(p.getUniqueId(), new PlayerStats(true));
        ConsoleMessage.debug(this.getClass(), plugin, "Default data was loaded for player " + ConsoleMessage.getPlayerString(p));
    }

    @Override
    public void save() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.save(p.getUniqueId());
        }
    }

    void save(UUID uuid) {

        Objects.requireNonNull(dataHandler, "dataHandler must not be null for save method.");

        PlayerStats stats = allStats.getOrDefault(uuid, new PlayerStats());
        if (stats.getAllowPhantomSpawn()) {
            dataHandler.deleteYmlFile(uuid.toString());
            ConsoleMessage.debug(this.getClass(), plugin, "Deleted file " + uuid.toString() + " because it had unnecessary data");
        } else {
            YamlConfiguration ymlFile = new YamlConfiguration();
            ymlFile.set(PlayerStats.configNames[0], stats.getAllowPhantomSpawn());
            dataHandler.saveYmlFile(uuid.toString(), ymlFile, true);
            ConsoleMessage.debug(this.getClass(), plugin, "Saved file " + uuid.toString());
        }

        allStats.remove(uuid);
        ConsoleMessage.debug(this.getClass(), plugin, "Unloaded data for UUID " + uuid.toString());
    }

    @NotNull
    public PlayerStats getPlayerStats(UUID uuid) {
        PlayerStats stats = allStats.get(uuid);
        if (stats == null) {
            allStats.put(uuid, new PlayerStats());
        }
        return allStats.get(uuid);
    }

    @Override
    public boolean shouldSpawnAsync(@NotNull Player p, @NotNull JavaPlugin plugin) {
        PlayerStats stats = allStats.getOrDefault(p.getUniqueId(), new PlayerStats());
        return stats.shouldSpawn(p, plugin);
    }

    @EventHandler // events only get registered if targeting should be prevented
    public void onTargetPrevent(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player && e.getEntityType().equals(EntityType.PHANTOM)) {
            if (!this.getPlayerStats(e.getTarget().getUniqueId()).getAllowPhantomSpawn()) {
                ((Phantom) e.getEntity()).setTarget(null);
                ConsoleMessage.debug(this.getClass(), plugin, "Prevented targeting of player " + e.getTarget().getName());
            }
        }
    }
}
