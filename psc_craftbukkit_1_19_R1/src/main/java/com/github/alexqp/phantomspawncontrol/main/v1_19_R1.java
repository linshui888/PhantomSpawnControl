package com.github.alexqp.phantomspawncontrol.main;

import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class v1_19_R1 extends InternalsProvider {

    @Override
    public Objective addPluginScoreboardObjective(Scoreboard scoreboard, String name) {
        return scoreboard.registerNewObjective(name, "minecraft.custom:minecraft.time_since_rest", name);
    }
}
