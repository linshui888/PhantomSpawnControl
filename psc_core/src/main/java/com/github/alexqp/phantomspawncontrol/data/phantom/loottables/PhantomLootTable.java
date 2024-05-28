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

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PhantomLootTable implements LootTable {

    private final NamespacedKey key;
    private final List<ItemStack> loot;
    //private final boolean considerFortune = false; // TODO future version
    //private final int exp = 0; // TODO future version

    public PhantomLootTable(@NotNull final NamespacedKey key, @NotNull final List<ItemStack> loot) {
        this.key = key;
        this.loot = new ArrayList<>(loot);
    }

    @NotNull
    public PhantomLootTable duplicate() {
        return new PhantomLootTable(key, loot);
    }

    public @NotNull List<ItemStack> getLoot() {
        return new ArrayList<>(this.loot);
    }

    @Override
    public @NotNull Collection<ItemStack> populateLoot(@NotNull Random random, @NotNull LootContext lootContext) {
        return new ArrayList<>(loot);
    }

    @Override
    public void fillInventory(@NotNull Inventory inventory, @NotNull Random random, @NotNull LootContext lootContext) {
        inventory.addItem(this.loot.toArray(new ItemStack[loot.size()]));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return "name = " + key.getKey() +
                ", loot = " + loot.toString();
    }
}
