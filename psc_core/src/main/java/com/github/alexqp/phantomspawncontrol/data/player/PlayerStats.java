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
