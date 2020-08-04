package com.github.alexqp.phantomspawncontrol.data.phantom.loottables;

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConfigurationSerializableCheckable;
import com.github.alexqp.commons.config.ConsoleErrorType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// synchronized!
public class AbstractPhantomLootTable implements ConfigurationSerializableCheckable {

    private static String[] configNames = {"children", "loot"};

    //private boolean considerFortune = false; // TODO future version
    //private int exp = 0; // TODO future version

    private HashSet<String> children = new HashSet<>();
    private final Object childrenLock = new Object();

    private List<ItemStack> loot = new ArrayList<>(); // not changeable

    AbstractPhantomLootTable() {}

    public AbstractPhantomLootTable(@NotNull Collection<ItemStack> loot) {
        this.loot = new ArrayList<>(loot);
    }

    AbstractPhantomLootTable(@NotNull Collection<ItemStack> loot, @NotNull Set<String> children) {
        this(loot);
        this.children = new HashSet<>(children);
    }

    public AbstractPhantomLootTable duplicate() {
        return new AbstractPhantomLootTable(this.getLoot(), this.getChildrenAsync());
    }

    /**
     * Gets the loot directly assigned to this LootTable.
     * @return a list of ItemStacks without null.
     */
    @NotNull
    public List<ItemStack> getLoot() {
        return new ArrayList<>(this.loot);
    }

    @NotNull
    public Set<String> getChildren() {
        return new HashSet<>(children);
    }

    @NotNull
    public Set<String> getChildrenAsync() {
        synchronized (childrenLock) {
            return new HashSet<>(children);
        }
    }

    /**
     * Gets the full loot of this LootTable including children.
     * @param map a map of AbstractPhantomLootTables (i. e. from container)
     * @param removeNotFound should not available children be completely removed from the LootTable?
     * @return a list of ItemStacks without null.
     */
    @NotNull
    public List<ItemStack> getLootAsync(@NotNull Map<String, @NotNull AbstractPhantomLootTable> map, boolean removeNotFound) {
        synchronized (childrenLock) {
            List<ItemStack> loot = this.getLoot();
            for (String child : new HashSet<>(children)) {
                AbstractPhantomLootTable lootTable = map.get(child);
                if (lootTable != null) {
                    loot.addAll(lootTable.getLoot());
                } else if (removeNotFound) {
                    children.remove(child);
                }
            }
            return loot;
        }
    }

    public boolean hasChild(@NotNull String child) {
        return children.contains(child);
    }

    // unsafe should only accessed by PhantomLootTableContainer
    // NEVER ADD ITSELF OR A CHILD'S PARENT AS CHILD!
    boolean addChildAsync(@NotNull String lootTable) {
        synchronized (childrenLock) {
            return this.children.add(Objects.requireNonNull(lootTable));
        }
    }

    public boolean removeChildAsync(String lootTable) {
        synchronized (childrenLock) {
            return this.children.remove(lootTable);
        }
    }

    @Override
    public boolean checkValues(ConfigChecker checker, ConfigurationSection section, String path, ConsoleErrorType errorType, boolean overwrite) {
        return true;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(configNames[0], children);
        map.put(configNames[1], loot);
        return map;
    }

    @SuppressWarnings({"unused", "unchecked"})
    @Nullable
    public static AbstractPhantomLootTable deserialize(Map<String, Object> map) {
        try {
            AbstractPhantomLootTable lootTable = new AbstractPhantomLootTable();
            lootTable.loot = new ArrayList<>((List<ItemStack>) map.getOrDefault(configNames[1], lootTable.loot));
            lootTable.children = (HashSet<String>) map.getOrDefault(configNames[0], new HashSet<>());
            return lootTable;
        }
        catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }
}
