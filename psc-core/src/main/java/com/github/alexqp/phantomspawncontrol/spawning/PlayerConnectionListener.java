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

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

class PlayerConnectionListener implements Listener {

    private static final String stopSpawnPermission = "phantomspawncontrol.stopspawn";

    public PlayerConnectionListener() {}

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        if (!e.getPlayer().hasPermission(stopSpawnPermission))
            SpawnRunnableAsync.startRunnable(e.getPlayer());
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent e) {
        SpawnRunnableAsync.stopRunnable(e.getPlayer());
    }
}
