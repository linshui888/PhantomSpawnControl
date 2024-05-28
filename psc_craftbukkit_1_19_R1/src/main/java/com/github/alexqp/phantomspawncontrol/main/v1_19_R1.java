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

package com.github.alexqp.phantomspawncontrol.main;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Phantom;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class v1_19_R1 extends InternalsProvider {

    @Override
    public Objective addPluginScoreboardObjective(Scoreboard scoreboard, String name) {
        return scoreboard.registerNewObjective(name, "minecraft.custom:minecraft.time_since_rest", name);
    }

    public Phantom spawnPhantom(@NotNull World world, @NotNull Location loc, @Nullable InternalsConsumer<Phantom> consumer) {
        if (consumer == null)
            return world.spawn(loc, Phantom.class);
        return world.spawn(loc, Phantom.class, consumer::accept);
    }
}
