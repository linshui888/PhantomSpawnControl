/*
 * Copyright (C) 2018-2024 Alexander Schmid
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.alexqp.phantomspawncontrol.command;

import com.github.alexqp.commons.command.AlexSubCommand;
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
