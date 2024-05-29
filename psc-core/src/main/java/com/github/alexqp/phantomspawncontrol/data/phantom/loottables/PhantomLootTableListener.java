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
