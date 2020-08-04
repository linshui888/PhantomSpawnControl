package com.github.alexqp.phantomspawncontrol.data.phantom.loottables;

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.dataHandler.DataHandler;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.data.Saveable;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PhantomLootTableContainer implements Saveable {

    private static final String fileName = "giantPhantomLootTables.yml";

    private final String metadataKey = "psc_lootTable";

    private JavaPlugin plugin;
    private DataHandler dataHandler;

    private ConcurrentHashMap<String, AbstractPhantomLootTable> abstractPhantomLootTables = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, PhantomLootTable> phantomLootTables = new ConcurrentHashMap<>(); // does not block retrieval!

    public PhantomLootTableContainer(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataHandler = new DataHandler(plugin);

        Bukkit.getPluginManager().registerEvents(new PhantomLootTableListener(metadataKey), plugin);
        ConsoleMessage.debug(this.getClass(), plugin, "Registered PhantomLootTableListener");
    }

    public String getMetadataKey() {
        return this.metadataKey;
    }

    @Nullable
    public PhantomLootTable getLootTable(@NotNull String name) {
        PhantomLootTable lootTable = phantomLootTables.get(name);
        return lootTable != null ? lootTable.duplicate() : null;
    }

    @Nullable
    public AbstractPhantomLootTable getAbstractLootTable(@NotNull String name) {
        AbstractPhantomLootTable original = abstractPhantomLootTables.get(name);
        return original != null ? original.duplicate() : null;
    }

    @NotNull
    public Set<String> getLootTableNames() {
        return phantomLootTables.keySet();
    }

    public void load(ConsoleErrorType errorType) {
        ConsoleMessage.debug(this.getClass(), plugin, "Loading LootTables...");
        ConsoleMessage.debug(this.getClass(), plugin, "Loading AbstractLootTables...");
        YamlConfiguration ymlFile = dataHandler.loadYmlFile(fileName);
        ConfigChecker configChecker = new ConfigChecker(plugin, ymlFile);

        for (String key : ymlFile.getKeys(false)) {

            AbstractPhantomLootTable lootTable = configChecker.checkSerializable(ymlFile, key, errorType, AbstractPhantomLootTable.class, true);

            if (lootTable == null) {
                ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, fileName, key, "Data is corrupt and was therefore skipped.");
                continue;
            }

            abstractPhantomLootTables.put(key, lootTable);
            ConsoleMessage.debug(this.getClass(), plugin, "Loaded AbstractLootTable " + key);
        }
        ConsoleMessage.debug(this.getClass(), plugin, "Completed loading of AbstractLootTables.");
        this.buildLootTablesAsync();
        ConsoleMessage.debug(this.getClass(), plugin, "Completed loading of LootTables.");
    }

    private void buildLootTablesAsync() {
        ConsoleMessage.debug(this.getClass(), plugin, "Building LootTables...");
        phantomLootTables.clear();
        for (String abstractName : abstractPhantomLootTables.keySet()) {
            this.updateLootTableAsync(abstractName);
        }
        ConsoleMessage.debug(this.getClass(), plugin, "Completed building of LootTables.");
    }

    public void updateLootTableAsync(@NotNull String name) {
        AbstractPhantomLootTable abstractLootTable = abstractPhantomLootTables.get(name);
        if (abstractLootTable != null) {
            PhantomLootTable lootTable = new PhantomLootTable(new NamespacedKey(plugin, name), abstractLootTable.getLootAsync(new ConcurrentHashMap<>(abstractPhantomLootTables), true));
            phantomLootTables.put(name, lootTable);
            ConsoleMessage.debug(this.getClass(), plugin, "Updated LootTable " + name);
            return;
        }
        ConsoleMessage.debug(this.getClass(), plugin, "Could not update LootTable " + name + " because it was not found.");
    }

    public boolean addLootTableAsync(@NotNull String name, @NotNull PhantomLootTable lootTable) throws IllegalArgumentException {
        if (abstractPhantomLootTables.containsKey(name))
            return false;

        for (ItemStack item : lootTable.getLoot()) {
            if (item == null) {
                throw new IllegalArgumentException("items must not be null");
            }
        }

        abstractPhantomLootTables.put(name, new AbstractPhantomLootTable(lootTable.getLoot()));
        this.updateLootTableAsync(name);
        return true;
    }

    public boolean editLootTableLootAsync(@NotNull String name, @NotNull List<ItemStack> loot) throws IllegalArgumentException {
        AbstractPhantomLootTable oldLootTable = abstractPhantomLootTables.get(name);
        if (oldLootTable != null) {
            for (ItemStack item : loot) {
                if (item == null)
                    throw new IllegalArgumentException("items must not be null");
            }
            AbstractPhantomLootTable newLootTable = new AbstractPhantomLootTable(loot, oldLootTable.getChildrenAsync());
            abstractPhantomLootTables.put(name, newLootTable);
            for (String abstractName : abstractPhantomLootTables.keySet()) {
                if (abstractPhantomLootTables.get(abstractName).hasChild(name))
                    this.updateLootTableAsync(abstractName);
            }
            return true;
        }
        return false;
    }

    // cautiously adds a child reference. See also AbstractPhantomLootTable#addChild
    public boolean addChildReferenceAsync(@NotNull String parent, @NotNull String child) {
        if (parent.equals(child))
            return false;

        AbstractPhantomLootTable abstractParent = abstractPhantomLootTables.get(parent);
        AbstractPhantomLootTable abstractChild = abstractPhantomLootTables.get(child);
        if (abstractParent != null && abstractChild != null) {
            if (abstractChild.hasChild(parent) || abstractParent.hasChild(child))
                return false;
            if (abstractParent.addChildAsync(child)) {
                this.updateLootTableAsync(parent);
                return true;
            }
        }
        return false;
    }

    public boolean removeChildReferenceAsync(@NotNull String parent, @NotNull String child) {
        AbstractPhantomLootTable abstractParent = abstractPhantomLootTables.get(parent);
        if (abstractParent != null && abstractParent.removeChildAsync(child)) {
            this.updateLootTableAsync(parent);
            return true;
        }
        return false;
    }

    /**
     * Removes a LootTable (also abstract) from this Container. Note: it will also remove all child-references.
     * @param name the lootTable's name
     * @return true if it was removed, false otherwise.
     */
    public boolean removeLootTable(@NotNull String name) {
        if (abstractPhantomLootTables.remove(name) != null) {
            ConsoleMessage.debug(this.getClass(), plugin, "Removing LootTable " + name + "...");
            phantomLootTables.remove(name);
            for (String abstractName : abstractPhantomLootTables.keySet()) {
                abstractPhantomLootTables.get(abstractName).removeChildAsync(name);
                ConsoleMessage.debug(this.getClass(), plugin, "Removed child-reference " + name + " from " + abstractName);
                this.updateLootTableAsync(abstractName);
            }
            return true;
        }
        return false;
    }

    @Override
    public void save() {
        ConsoleMessage.debug(this.getClass(), plugin, "Saving AbstractLootTables...");
        YamlConfiguration ymlFile = new YamlConfiguration();
        for (String name : abstractPhantomLootTables.keySet()) {
            ymlFile.set(name, abstractPhantomLootTables.get(name));
            ConsoleMessage.debug(this.getClass(), plugin, "Prepared AbstractLootTable " + name + " for saving...");
        }
        dataHandler.saveYmlFile(fileName, ymlFile, true);
        ConsoleMessage.debug(this.getClass(), plugin, "Saved AbstractLootTables.");
    }
}
