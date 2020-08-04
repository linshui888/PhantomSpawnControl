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
