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

package com.github.alexqp.phantomspawncontrol.utility;

import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChestContent {

    /**
     * Gets the chestContents
     * @param p the player
     * @return a none-null list or null if targetBlock is no chest
     */
    @Nullable
    public static List<ItemStack> getTargetContents(@NotNull Player p, int range) {
        BlockState target = p.getTargetBlock(null, range).getState();
        if (target instanceof Chest) {
            List<ItemStack> contents = new ArrayList<>(Arrays.asList(((Chest) target).getBlockInventory().getStorageContents()));
            contents.removeIf(Objects::isNull);
            return contents;
        }
        return null;
    }

    @Nullable
    public static List<ItemStack> getTargetContents(@NotNull Player p) {
        return ChestContent.getTargetContents(p, 5);
    }
}
