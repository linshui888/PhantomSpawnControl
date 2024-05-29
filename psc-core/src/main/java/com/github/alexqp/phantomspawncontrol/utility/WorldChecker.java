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

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WorldChecker {

    private HashSet<World.Environment> enabledWorldEnvs;
    private HashSet<String> disabledWorldNames;

    public WorldChecker(Collection<? extends World.Environment> enabledEnvironments, Collection<? extends String> disabledWorldNames) {
        this.enabledWorldEnvs = new HashSet<>(enabledEnvironments);
        this.disabledWorldNames = new HashSet<>(disabledWorldNames);
    }

    public boolean isWorldEnabled(@NotNull World world) {
        return enabledWorldEnvs.contains(world.getEnvironment()) && !disabledWorldNames.contains(world.getName());
    }

    @NotNull
    public static Set<World.Environment> getEnabledEnvsBySection(@Nullable ConfigurationSection section) {
        Set<World.Environment> set = new HashSet<>();
        if (section == null)
            return set;

        for (World.Environment env : World.Environment.values()) {

            if (section.getBoolean(env.name().toLowerCase(), false)) {
                set.add(env);
            }
        }

        return set;
    }
}
