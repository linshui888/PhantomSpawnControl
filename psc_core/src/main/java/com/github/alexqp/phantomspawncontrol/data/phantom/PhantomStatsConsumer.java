package com.github.alexqp.phantomspawncontrol.data.phantom;

import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.main.InternalsError;
import com.github.alexqp.phantomspawncontrol.main.InternalsProvider;
import org.bukkit.entity.Phantom;
import org.bukkit.loot.LootTable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// thread safe due to no setters
public class PhantomStatsConsumer implements Consumer<Phantom> {

    private JavaPlugin plugin;
    private InternalsProvider internalsProvider;

    private PhantomStats stats;

    private String metadataKey;
    private LootTable lootTable;

    PhantomStatsConsumer(@NotNull JavaPlugin plugin, @NotNull InternalsProvider internals,
                         @Nullable PhantomStats stats,
                         @Nullable String metadataKey, @Nullable LootTable lootTable)
                throws IllegalArgumentException {
        this.plugin = plugin;
        this.internalsProvider = internals;

        this.stats = stats;

        this.metadataKey = metadataKey;
        this.lootTable = lootTable;

        if (metadataKey == null && lootTable != null) {
            throw new IllegalArgumentException("metadataKey must not be null while lootTable is not null");
        }
    }

    public boolean hasStats() {
        return stats != null;
    }

    /*public boolean hasLootTable() {
        return stats != null && lootTable != null;
    }*/

    @Override
    public String toString() {
        if (stats != null) {
            StringBuilder string = new StringBuilder();
            string.append(stats.toString());
            if (lootTable != null) {
                string.append(", activeLootTable = ").append(lootTable.getKey().getKey());
            }
            return string.toString();
        }
        return "No custom stats";
    }

    @Override
    public void accept(Phantom phantom) {
        if (stats != null) {
            try {
                internalsProvider.applyPhantomStats(stats, phantom);
                ConsoleMessage.debug(this.getClass(), plugin, "Applied PhantomStats.");
                if (lootTable != null) {
                    phantom.setLootTable(null); // wait for API change...
                    phantom.setMetadata(metadataKey, new FixedMetadataValue(plugin, lootTable));
                    ConsoleMessage.debug(this.getClass(), plugin, "Added LootTable to phantom.");
                }
            } catch (InternalsError e) {
                ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, "Error while applying phantom stats. Please contact developer.");
                ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, "Error message: " + e.getMessage());
                ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, "Error Version: " + e.getVersion());
            }
        }
    }
}
