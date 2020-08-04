package com.github.alexqp.phantomspawncontrol.data.player;

import com.github.alexqp.commons.config.ConsoleErrorType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerConnectionListener implements Listener {

    private PlayerStatsContainer playerStatsContainer;

    public PlayerConnectionListener(@NotNull PlayerStatsContainer container) {
        this.playerStatsContainer = container;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        playerStatsContainer.load(ConsoleErrorType.NONE, e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerStatsContainer.save(e.getPlayer().getUniqueId());
    }
}
