package com.github.alexqp.phantomspawncontrol.data.phantom.loottables;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.loot.LootContext;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class PhantomLootTableListener implements Listener {

    private final String metadataKey;

    public PhantomLootTableListener(@NotNull String metadataKey) {
        this.metadataKey = metadataKey;
    }

    @EventHandler
    public void onPhantomDeath(EntityDeathEvent e) {
        if (e.getEntityType().equals(EntityType.PHANTOM)) {
            Phantom phantom = (Phantom) e.getEntity();
            if (phantom.hasMetadata(metadataKey)) {
                for (MetadataValue metadataValue : phantom.getMetadata(metadataKey)) {
                    if (metadataValue.value() instanceof PhantomLootTable) {
                        LootContext.Builder lootContext = new LootContext.Builder(phantom.getLocation()).lootedEntity(phantom);
                        e.getDrops().clear();
                        e.getDrops().addAll(((PhantomLootTable) Objects.requireNonNull(metadataValue.value())).populateLoot(new Random(), lootContext.build()));
                    }
                }

            }
        }
    }
}
