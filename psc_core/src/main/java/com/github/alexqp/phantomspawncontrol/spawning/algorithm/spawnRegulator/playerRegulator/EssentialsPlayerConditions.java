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

package com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.playerRegulator;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.spawning.algorithm.spawnRegulator.PlayerSpawnRegulator;
import com.github.alexqp.phantomspawncontrol.utility.SpawnCancelMsg;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;

public class EssentialsPlayerConditions implements PlayerSpawnRegulator {

    @Nullable
    public static EssentialsPlayerConditions build(final @NotNull JavaPlugin plugin, final @NotNull ConfigurationSection playerSection) {
        ConfigChecker configChecker = new ConfigChecker(plugin);

        String sectionPath = "essentials";

        ConfigurationSection section = configChecker.checkConfigSection(playerSection, sectionPath, ConsoleErrorType.ERROR);
        if (section != null) {
            try {

                Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

                boolean afkProtection = configChecker.checkBoolean(section, "afk_protection", ConsoleErrorType.WARN, false);
                boolean vanishProtection = configChecker.checkBoolean(section, "vanish_protection", ConsoleErrorType.WARN, false);

                if (afkProtection || vanishProtection) {

                    if (essentials != null) {
                        return new EssentialsPlayerConditions(essentials, afkProtection, vanishProtection);
                    } else {
                        ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, playerSection.getCurrentPath(), sectionPath, "At least one Essentials feature was enabled while Essentials itself is not.");
                    }
                }

            }
            catch (ClassCastException e) {
                ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, "You have another plugin named \"Essentials\" installed which is not supported.");
                return null;
            }

        }

        return null;
    }

    private Essentials essentials;

    private boolean afkProtection;
    private boolean vanishProtection;

    private EssentialsPlayerConditions(final @NotNull Essentials essentials, final boolean afkProtection, final boolean vanishProtection) {
        this.essentials = essentials;
        this.afkProtection = afkProtection;
        this.vanishProtection = vanishProtection;
    }

    @Override
    public boolean shouldSpawnAsync(@NotNull Player p, @NotNull JavaPlugin plugin) {
        try {
            return Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                User user = essentials.getUser(p.getUniqueId());

                if (afkProtection && user.isAfk()) {
                    ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "Essentials-AFK"));
                    return false;
                }

                if (vanishProtection && user.isVanished()) {
                    ConsoleMessage.debug(this.getClass(), plugin, SpawnCancelMsg.build(p, "Essentials-Vanish"));
                    return false;
                }
                return true;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            SpawnCancelMsg.printFutureGetError(plugin, this, e);
        }
        return false;
    }
}
