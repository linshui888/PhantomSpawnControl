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
