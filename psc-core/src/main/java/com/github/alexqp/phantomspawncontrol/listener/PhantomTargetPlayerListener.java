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

package com.github.alexqp.phantomspawncontrol.listener;

import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.data.player.PlayerStatsContainer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PhantomTargetPlayerListener implements Listener {

    private final JavaPlugin plugin;
    private final PlayerStatsContainer container;

    public PhantomTargetPlayerListener(@NotNull JavaPlugin plugin, @NotNull PlayerStatsContainer container) {
        this.plugin = Objects.requireNonNull(plugin);
        this.container = Objects.requireNonNull(container);
    }

    @EventHandler (ignoreCancelled = true)
    public void onTargetPrevent(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player && e.getEntityType().equals(EntityType.PHANTOM)) {
            if (!container.getPlayerStats(e.getTarget().getUniqueId()).getAllowPhantomSpawn()) {
                e.setCancelled(true);
                ConsoleMessage.debug(this.getClass(), plugin, "Prevented targeting of player " + e.getTarget().getName());
            } else {
                ConsoleMessage.debug(this.getClass(), plugin, "Did not prevent targeting of player " + e.getTarget().getName());
            }
        }
    }
}
