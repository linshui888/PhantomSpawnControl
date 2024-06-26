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
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStatsConsumer;
import com.github.alexqp.phantomspawncontrol.main.InternalsProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class SummonSubCmd extends AsyncContainerSubCmd {

    private BaseComponent success = new TextComponent(new ComponentBuilder("Successfully summoned a giant_phantom.").color(ChatColor.DARK_GREEN).create());
    private BaseComponent notFound = new TextComponent(new ComponentBuilder("There is no giant_phantom with that score!").color(ChatColor.RED).create());

    private final InternalsProvider internals;

    SummonSubCmd(@NotNull GiantPhantomsSubCmd parent, @NotNull InternalsProvider internals) {
        super("summon", new TextComponent("summons a giant_phantom"), parent);
        TextComponent cmdParamLine = new TextComponent("<score>");
        this.internals = internals;
        this.setCmdParamLine(cmdParamLine);
        this.setIsConsoleCmd(false);
        this.makeFinal();
    }

    @Override
    public void makeFinal() {
        this.internalMakeFinal();
        this.success = this.getPrefixMessage(success);
        this.notFound = this.getPrefixMessage(notFound);
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
                        PhantomStatsConsumer consumer = container.getPhantomStatsConsumerAsync(score);
                        if (!consumer.hasStats()) {
                            sendMessage(sender, notFound);
                        } else {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Player p = (Player) sender;
                                    internals.spawnPhantom(p.getWorld(), p.getLocation(), consumer);
                                    sendMessage(p, success);
                                    sendMessage(p, new ComponentBuilder(consumer.toString()).color(ChatColor.DARK_GREEN).create());
                                }
                            }.runTask(plugin);
                        }
                        return;
                    }
                }
                catch (NumberFormatException ignored) {}
                sendMessage(sender, getUsageLine(label));
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    @Override
    protected @NotNull List<String> additionalTabCompleterOptions(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        return new ArrayList<>(GiantPhantomsSubCmd.translateIntegerToString(container.getDefinedScores()));
    }
}
