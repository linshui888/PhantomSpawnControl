package com.github.alexqp.phantomspawncontrol.spawning;

import com.github.alexqp.commons.messages.ConsoleMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Phantom;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;

public class DisableNaturalPhantomSpawning implements Listener {

    public DisableNaturalPhantomSpawning(JavaPlugin plugin) {
        ConsoleMessage.debug(this.getClass(), plugin, "Disabled natural phantom spawning");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPhantomSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Phantom && e.getSpawnReason().equals(SpawnReason.NATURAL))
            e.setCancelled(true);
    }
}
