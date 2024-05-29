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

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ConfigReader {

    @NotNull
    public static Set<GameMode> getEnabledGameModes(final ConfigChecker configChecker, final ConfigurationSection section, final String path) {
        Set<GameMode> set = new HashSet<>();

        ConfigurationSection gmSection = configChecker.checkConfigSection(section, path, ConsoleErrorType.ERROR);
        if (gmSection != null) {

            for (GameMode gm : GameMode.values()) {

                if (configChecker.checkBoolean(gmSection, gm.name().toLowerCase(), ConsoleErrorType.WARN, false)) {
                    set.add(gm);
                }
            }
        }

        return set;
    }
}
