package com.github.alexqp.phantomspawncontrol.spawning;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

class PlayerConnectionListener implements Listener {

    private static final String stopSpawnPermission = "phantomspawncontrol.stopspawn";

    public PlayerConnectionListener() {}

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        if (!e.getPlayer().hasPermission(stopSpawnPermission))
            SpawnRunnableAsync.startRunnable(e.getPlayer());
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent e) {
        SpawnRunnableAsync.stopRunnable(e.getPlayer());
    }
}
