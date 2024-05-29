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

import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpawnCancelMsg {

    public static String build(Player p, String reason) {
        return "Spawn will be cancelled for player " + ConsoleMessage.getPlayerString(p) + " over " + reason;
    }

    public static String build(Location loc, String reason) {
        String worldName = "null";
        if (loc.getWorld() != null) {
            try {
                worldName = loc.getWorld().getName();
            }
            catch (IllegalArgumentException ignored) {}
        }
        return "Spawn will be cancelled in world " + worldName + " at " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " over " + reason;
    }

    public static void printFutureGetError(@NotNull JavaPlugin plugin, @NotNull Object obj, @NotNull Exception e) {
        SpawnCancelMsg.printFutureGetError(plugin, obj, null, e);
    }

    public static void printFutureGetError(@NotNull JavaPlugin plugin, @NotNull Object obj, @Nullable String devComment, @NotNull Exception e) {
        ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, "Future Get threw error in class " + obj.getClass().getSimpleName() + "(" + devComment + ")");
        e.printStackTrace();
    }
}
