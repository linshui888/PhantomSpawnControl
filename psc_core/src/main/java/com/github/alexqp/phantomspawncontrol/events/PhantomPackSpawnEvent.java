package com.github.alexqp.phantomspawncontrol.events;

import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class PhantomPackSpawnEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private HashSet<Phantom> pack;
    private Player target;
    private int nextSpawnDelay;

    public PhantomPackSpawnEvent(@NotNull HashSet<Phantom> pack, @NotNull Player target, int nextSpawnDelay) {
        super();

        this.pack = pack;
        this.target = target;
        this.nextSpawnDelay = nextSpawnDelay;
    }

    @NotNull
    public HashSet<Phantom> getPack() {
        return this.pack;
    }

    @NotNull
    public Player getTarget() {
        return this.target;
    }

    public int getNextSpawnDelay() {
        return this.nextSpawnDelay;
    }

    public void setNextSpawnDelay(int delay) {
        this.nextSpawnDelay = delay;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
