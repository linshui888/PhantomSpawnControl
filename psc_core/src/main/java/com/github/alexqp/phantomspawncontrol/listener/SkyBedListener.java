package com.github.alexqp.phantomspawncontrol.listener;


import com.github.alexqp.phantomspawncontrol.spawning.algorithm.SpawnAlgorithmAsync;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class SkyBedListener implements Listener {

    public SkyBedListener(JavaPlugin plugin, SpawnAlgorithmAsync spawnAlgorithm) {
        // TODO future version
        // spawnCooldown und delay wird ggf ein Problem!
        // es ex. spawnAlgorithm.getSpawnLocations ohne checks
        // spawning einheitlich in eine eigene Klasse iwie packen?
    }
}
