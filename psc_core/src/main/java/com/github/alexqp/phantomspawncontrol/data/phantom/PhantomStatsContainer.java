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

package com.github.alexqp.phantomspawncontrol.data.phantom;

import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.data.Saveable;
import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.dataHandler.DataHandler;
import com.github.alexqp.phantomspawncontrol.data.phantom.loottables.AbstractPhantomLootTable;
import com.github.alexqp.phantomspawncontrol.data.phantom.loottables.PhantomLootTable;
import com.github.alexqp.phantomspawncontrol.data.phantom.loottables.PhantomLootTableContainer;
import com.github.alexqp.phantomspawncontrol.main.InternalsProvider;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class PhantomStatsContainer extends Observable implements Saveable {

    private static final String fileName = "giantPhantomsConfiguration.yml";

    private final JavaPlugin plugin;
    private final InternalsProvider internals;
    private final DataHandler dataHandler;

    private final PhantomLootTableContainer lootTableContainer;

    private final ConcurrentSkipListMap<Integer, PhantomStats> phantoms = new ConcurrentSkipListMap<>();

    public PhantomStatsContainer(@NotNull JavaPlugin plugin, @NotNull InternalsProvider internals) {
        this.plugin = plugin;
        this.internals = internals;
        this.dataHandler = new DataHandler(plugin);
        this.lootTableContainer = new PhantomLootTableContainer(plugin);
    }

    private YamlConfiguration buildFile() {
        YamlConfiguration ymlFile = new YamlConfiguration();
        ymlFile.set("0", new PhantomStats());
        ymlFile.set("24000", new PhantomStats(3, 25, 1, 20, 11));
        ymlFile.set("48000", new PhantomStats(5, 30, 1.3, 24, 13));
        ymlFile.set("72000", new PhantomStats(7, 35, 2, 28, 18));
        ymlFile.set("208000", new PhantomStats(9, 50, 4, 28, 30));
        return ymlFile;
    }
    public void load(ConsoleErrorType errorType) {
        ConsoleMessage.debug(this.getClass(), plugin, "Loading PhantomStats...");
        YamlConfiguration ymlFile = dataHandler.loadYmlFile(fileName);
        ConfigChecker configChecker = new ConfigChecker(plugin, ymlFile);

        if (ymlFile.getKeys(false).isEmpty()) {
            ConsoleMessage.send(ConsoleErrorType.WARN, plugin, "There was no file " + fileName + ". A default file will be generated.");
            ymlFile = this.buildFile();
        }

        phantoms.clear();

        int intKey;
        for (String key : ymlFile.getKeys(false)) {
            try {
                intKey = Integer.parseInt(key);
            }
            catch (IllegalArgumentException e) {
                ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, fileName, key, "path is not a valid integer and was therefore skipped");
                continue;
            }

            PhantomStats phantomStats = configChecker.checkSerializable(ymlFile, key, errorType, PhantomStats.class, true);

            if (phantomStats == null) {
                ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, fileName, key, "Data is corrupt and was therefore skipped.");
                continue;
            }
            phantoms.put(intKey, phantomStats);
            ConsoleMessage.debug(this.getClass(), plugin, "Loaded Phantom " + key + " with stats " + phantomStats.toString());
        }

        lootTableContainer.load(errorType);
        ConsoleMessage.debug(this.getClass(), plugin, "Completed loading of PhantomStats.");
    }
    @Override
    public void save() {
        ConsoleMessage.debug(this.getClass(), plugin, "Saving PhantomStats...");
        YamlConfiguration ymlFile = new YamlConfiguration();

        for (Integer key : phantoms.keySet()) {
            ymlFile.set(key.toString(), phantoms.get(key));
            ConsoleMessage.debug(this.getClass(), plugin, "Prepared Phantom " + key + " for saving...");
        }
        dataHandler.saveYmlFile(fileName, ymlFile, true);
        lootTableContainer.save();
        ConsoleMessage.debug(this.getClass(), plugin, "Saved PhantomStats.");
    }

    public @NotNull Set<Integer> getDefinedScores() {
        return phantoms.keySet();
    }

    public @Nullable PhantomStats getPhantomStats(int subtractedScore) {
        return phantoms.get(subtractedScore);
    }
    private @NotNull PhantomStats getPhantomStatsAsync(int subtractedScore, @NotNull Random random) {
        TreeMap<Integer, PhantomStats> chances = new TreeMap<>();
        synchronized (this) {
            for (int key : phantoms.keySet()) {
                int chance = subtractedScore - key + 1;

                if (chance <= 0)
                    continue;
                // phantom not reached yet

                if (chances.higherKey(key) == null || subtractedScore < chances.higherKey(key)) {
                    chance = subtractedScore - key;
                } else {
                    chance = chances.higherKey(key) - key;
                } // higher key was not / was reached.

                if (!chances.isEmpty())
                    chance = chances.lastKey() + chance; // shifting...

                chances.put(chance, phantoms.get(key).copy());
            }
        }

        if (chances.isEmpty()) {
            ConsoleMessage.debug(this.getClass(), plugin, "No potential PhantomStats for " + subtractedScore + ", using defaultPhantom");
            return new PhantomStats();
        }
        int randomInt = random.nextInt(chances.lastKey() + 1);
        int ceilingKey = chances.ceilingKey(randomInt);

        PhantomStats result = chances.get(ceilingKey);
        if (result == null) {
            ConsoleMessage.debug(this.getClass(), plugin, "PhantomStats null for " + subtractedScore + ", using defaultPhantom");
            return new PhantomStats();
        }

        return result;
    }

    public @NotNull PhantomStatsConsumer getPhantomStatsConsumerAsync(int subtractedScore) {
        PhantomStats stats = this.getPhantomStats(subtractedScore);
        LootTable lootTable = null;
        if (stats != null) {
            lootTable = lootTableContainer.getLootTable(stats.getLootTableKeyAsync(new Random()));
        }
        return new PhantomStatsConsumer(plugin, internals, stats, lootTableContainer.getMetadataKey(), lootTable);
    }
    public @NotNull PhantomStatsConsumer getPhantomStatsConsumerAsync(int subtractedScore, @NotNull Random random) {
        PhantomStats stats = this.getPhantomStatsAsync(subtractedScore, random);
        LootTable lootTable = lootTableContainer.getLootTable(stats.getLootTableKeyAsync(random));
        return new PhantomStatsConsumer(plugin, internals, stats, lootTableContainer.getMetadataKey(), lootTable);
    }


    public boolean addPhantomStatsAsync(int subtractedScore, @NotNull PhantomStats stats) {
        Objects.requireNonNull(stats, "PhantomStats must not be null");
        if (phantoms.containsKey(subtractedScore))
            return false;

        phantoms.put(subtractedScore, stats);
        this.setChanged();
        this.notifyObservers();
        return true;
    }
    public boolean removePhantomStatsAsync(int subtractedScore) {
        if (phantoms.remove(subtractedScore) != null) {
            this.setChanged();
            this.notifyObservers();
            return true;
        }
        return false;
    }

    @NotNull
    public Set<String> getRegisteredLootTables() {
        return lootTableContainer.getLootTableNames();
    }
    @Nullable
    public PhantomLootTable getLootTable(@NotNull String name) {
        return this.lootTableContainer.getLootTable(name);
    }

    public boolean addLootTableToPhantomStatsAsync(int subtractedScore, @NotNull String lootTableName, int weight) {
        PhantomStats stats = phantoms.get(subtractedScore);
        if (stats != null)
            return stats.addLootTableKeyAsync(lootTableName, weight, lootTableContainer.getLootTableNames());
        return false;
    }
    public boolean removeLootTableFromPhantomStatsAsync(int subtractedScore, @NotNull String lootTableName) {
        PhantomStats stats = phantoms.get(subtractedScore);
        if (stats != null) {
            return stats.removeLootTableKeyAsync(lootTableName);
        }
        return false;
    }

    public boolean registerLootTableAsync(@NotNull String name, @NotNull PhantomLootTable lootTable) {
        if (lootTableContainer.addLootTableAsync(name, lootTable)) {
            this.setChanged();
            this.notifyObservers();
            ConsoleMessage.debug(this.getClass(), plugin, "Registered lootTable " + name);
            return true;
        }
        return false;
    }
    public boolean unregisterLootTableAsync(String name) {
        if (lootTableContainer.removeLootTable(name)) {
            for (PhantomStats stats : phantoms.values()) {
                stats.removeLootTableKeyAsync(name);
                ConsoleMessage.debug(this.getClass(), plugin, "Removed lootTable reference from phantomStats " + stats.toString());
            }
            this.setChanged();
            this.notifyObservers();
            ConsoleMessage.debug(this.getClass(), plugin, "Unregistered lootTable " + name);
            return true;
        }
        return false;
    }
    public boolean editLootOfLootTableAsync(@NotNull String name, @NotNull List<ItemStack> loot) throws IllegalArgumentException {
        return lootTableContainer.editLootTableLootAsync(name, loot);
    }

    @Nullable
    public Set<String> getChildReferencesOfLootTable(@NotNull String parent) {
        AbstractPhantomLootTable abstractPhantomLootTable = lootTableContainer.getAbstractLootTable(parent);
        return abstractPhantomLootTable != null ? abstractPhantomLootTable.getChildren() : null;
    }

    public boolean addChildReferenceToLootTableAsync(@NotNull String parent, @NotNull String child) {
        return lootTableContainer.addChildReferenceAsync(parent, child);
    }
    public boolean removeChildReferenceFromLootTableAsync(@NotNull String parent, @NotNull String child) {
        return lootTableContainer.removeChildReferenceAsync(parent, child);
    }

}
