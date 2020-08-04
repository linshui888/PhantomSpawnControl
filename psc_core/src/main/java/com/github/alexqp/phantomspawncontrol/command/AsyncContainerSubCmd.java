package com.github.alexqp.phantomspawncontrol.command;

import com.github.alexqp.commons.command.better.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStatsContainer;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class AsyncContainerSubCmd extends AlexSubCommand {

    protected final @NotNull JavaPlugin plugin;
    protected final @NotNull PhantomStatsContainer container;

    protected AsyncContainerSubCmd(@NotNull String name, @NotNull TextComponent helpLine, @NotNull AlexSubCommand parent, @NotNull JavaPlugin plugin, @NotNull PhantomStatsContainer container) {
        super(name, helpLine, parent);
        this.plugin = plugin;
        this.container = container;
    }

    protected AsyncContainerSubCmd(@NotNull String name, @NotNull TextComponent helpLine, @NotNull AsyncContainerSubCmd parent) {
        super(name, helpLine, parent);
        this.plugin = parent.plugin;
        this.container = parent.container;
    }
}
