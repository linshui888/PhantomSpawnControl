package com.github.alexqp.phantomspawncontrol.listener;

import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.data.player.PlayerStatsContainer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PhantomTargetPlayerListener implements Listener {

    private final JavaPlugin plugin;
    private final PlayerStatsContainer container;

    public PhantomTargetPlayerListener(@NotNull JavaPlugin plugin, @NotNull PlayerStatsContainer container) {
        this.plugin = Objects.requireNonNull(plugin);
        this.container = Objects.requireNonNull(container);
    }

    @EventHandler (ignoreCancelled = true)
    public void onTargetPrevent(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player && e.getEntityType().equals(EntityType.PHANTOM)) {
            if (!container.getPlayerStats(e.getTarget().getUniqueId()).getAllowPhantomSpawn()) {
                e.setCancelled(true);
                ConsoleMessage.debug(this.getClass(), plugin, "Prevented targeting of player " + e.getTarget().getName());
            } else {
                ConsoleMessage.debug(this.getClass(), plugin, "Did not prevent targeting of player " + e.getTarget().getName());
            }
        }
    }
}
