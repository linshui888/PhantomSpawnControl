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

package com.github.alexqp.phantomspawncontrol.command.giantPhantoms;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class ListSubCmd extends AsyncContainerSubCmd {

    private BaseComponent header = new TextComponent("List of all giant_phantoms:");

    ListSubCmd(@NotNull GiantPhantomsSubCmd parent) {
        super("list", new TextComponent("lists all giant_phantoms"), parent);
        this.makeFinal();
    }

    @Override
    public void makeFinal() {
        this.internalMakeFinal();
        this.header = this.getPrefixMessage(header);
    }

    private BaseComponent getScoreLine(int score) {
        return new TextComponent(new ComponentBuilder().color(ChatColor.DARK_RED).append("Score = " + score + ": ").append(Objects.requireNonNull(container.getPhantomStats(score)).toString()).reset().create());
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label,
                              @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments,
                              @NotNull String[] args, int startIndex) {
        new BukkitRunnable() {

            @Override
            public void run() {
                sendMessage(sender, header);
                Set<Integer> scores = container.getDefinedScores();
                for (int score : scores) {
                    sendMessage(sender, getScoreLine(score));
                }
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }
}
