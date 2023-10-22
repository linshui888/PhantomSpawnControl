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
