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

package com.github.alexqp.phantomspawncontrol.data.player;

import com.github.alexqp.phantomspawncontrol.spawning.algorithm.SpawnConditionsHandler;
import com.github.alexqp.phantomspawncontrol.utility.SpawnCancelMsg;
import com.github.alexqp.commons.messages.ConsoleMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerStats implements SpawnConditionsHandler {

    static final String[] configNames = {"allow_spawn"};

    private boolean allowPhantomSpawn = true;

    PlayerStats() {
    }

    PlayerStats(boolean allowPhantomSpawn) {
        this.allowPhantomSpawn = allowPhantomSpawn;
    }

    public boolean getAllowPhantomSpawn() {
        return this.allowPhantomSpawn;
    }

    public boolean toggleAllowPhantomSpawn() {
        allowPhantomSpawn = !allowPhantomSpawn;
        return allowPhantomSpawn;
    }

    @Override
    public boolean shouldSpawn(Player p, JavaPlugin plugin) {
        if (!allowPhantomSpawn) {
            ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, configNames[0]));
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "allow_spawn = " + this.getAllowPhantomSpawn();
    }
}
