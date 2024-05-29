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
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStat;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStats;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class AddSubCmd extends AsyncContainerSubCmd {

    private TextComponent invalidValueAtError = new TextComponent(new ComponentBuilder("Invalid value at").color(ChatColor.RED).create());
    private BaseComponent existError = new TextComponent(new ComponentBuilder("There is already a giant_phantom with that score!").color(ChatColor.RED).create());
    private BaseComponent success = new TextComponent(new ComponentBuilder("Giant_Phantom was successfully added.").color(ChatColor.DARK_GREEN).create());

    AddSubCmd(@NotNull GiantPhantomsSubCmd parent) {
        super("add", new TextComponent("adds a giant_phantom"), parent);

        TextComponent cmdParamLine = new TextComponent("<score>");
        for (PhantomStat<?> option : PhantomStat.values()) {
            cmdParamLine.addExtra(" [<" + option.getConfigName() + ">]");
        }
        this.setCmdParamLine(cmdParamLine);
        this.makeFinal();
    }

    @Override
    public void makeFinal() {
        this.internalMakeFinal();
        this.invalidValueAtError = new TextComponent(this.getPrefixMessage(invalidValueAtError));
        this.existError = this.getPrefixMessage(existError);
        this.success = this.getPrefixMessage(success);
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label,
                           @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments,
                           @NotNull String[] args, int startIndex) {

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (args.length > startIndex) {

                        int score = Integer.parseInt(args[startIndex]);
                        PhantomStats stats = new PhantomStats();

                        for (int i = 0, j = startIndex + 1; i < PhantomStat.values().size() && j < args.length; i++, j++) {
                            PhantomStat<?> option = PhantomStat.values().get(i);
                            if (!stats.setByOptionAsync(option, args[j])) {
                                sendMessage(sender, new ComponentBuilder(invalidValueAtError).append(" " + option.getConfigName()).create());
                            }
                        }

                        if (!container.addPhantomStatsAsync(score, stats)) {
                            sendMessage(sender, existError);
                            return;
                        }
                        sendMessage(sender, success);
                        sendMessage(sender, new ComponentBuilder(stats.toString()).color(ChatColor.GREEN).create());
                        return;
                    }
                }
                catch (NumberFormatException ignored) {}
                sendMessage(sender, getUsageLine(label));
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }
}
