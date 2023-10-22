package com.github.alexqp.phantomspawncontrol.main;

import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Phantom;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class v1_20_R1 extends InternalsProvider {

    @Override
    public Objective addPluginScoreboardObjective(Scoreboard scoreboard, String name) {
        return scoreboard.registerNewObjective(name, Criteria.statistic(Statistic.TIME_SINCE_REST), name);
    }

    public Phantom spawnPhantom(@NotNull World world, @NotNull Location loc, @Nullable InternalsConsumer<Phantom> consumer) {
        if (consumer == null)
            return world.spawn(loc, Phantom.class);
        return world.spawn(loc, Phantom.class, consumer::accept);
    }
}
