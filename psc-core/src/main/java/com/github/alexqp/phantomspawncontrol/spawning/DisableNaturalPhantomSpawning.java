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

package com.github.alexqp.phantomspawncontrol.spawning;

import com.github.alexqp.commons.messages.ConsoleMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Phantom;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;

public class DisableNaturalPhantomSpawning implements Listener {

    public DisableNaturalPhantomSpawning(JavaPlugin plugin) {
        ConsoleMessage.debug(this.getClass(), plugin, "Disabled natural phantom spawning");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPhantomSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Phantom && e.getSpawnReason().equals(SpawnReason.NATURAL))
            e.setCancelled(true);
    }
}
