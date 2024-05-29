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

package com.github.alexqp.phantomspawncontrol.command.lootTables;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class RemoveSubCmd extends AsyncContainerSubCmd {

    private BaseComponent success = new TextComponent(new ComponentBuilder("Successfully removed lootTable.").color(ChatColor.DARK_GREEN).create());
    private BaseComponent notFound = new TextComponent(new ComponentBuilder("There is no lootTable with that name!").color(ChatColor.RED).create());

    RemoveSubCmd(@NotNull LootTablesSubCmd parent) {
        super("remove", new TextComponent("removes a lootTable"), parent);
        this.setCmdParamLine(new TextComponent("<name>"));
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
                if (args.length > startIndex) {
                    if (container.unregisterLootTableAsync(args[startIndex])) {
                        sendMessage(sender, success);
                    } else {
                        sendMessage(sender, notFound);
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    @Override
    protected @NotNull List<String> additionalTabCompleterOptions(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        return new ArrayList<>(container.getRegisteredLootTables());
    }
}
