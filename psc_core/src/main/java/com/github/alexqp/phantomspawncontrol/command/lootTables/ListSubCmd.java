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
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class ListSubCmd extends AsyncContainerSubCmd {

    ListSubCmd(@NotNull LootTablesSubCmd parent) {
        super("list", new TextComponent("lists all lootTables"), parent);
        this.makeFinal();
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label,
                              @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments,
                              @NotNull String[] args, int startIndex) {
        new BukkitRunnable() {

            @Override
            public void run() {
                TextComponent msg = new TextComponent("LootTables: ");
                List<String> lootTables = new ArrayList<>(container.getRegisteredLootTables());
                // TODO we need abstractLootTables and children!
                for (int i = 0; i + 1 < lootTables.size(); i++) {
                    msg.addExtra(lootTables.get(i) + ", ");
                }
                if (lootTables.size() != 0) {
                    msg.addExtra(lootTables.get(lootTables.size() - 1));
                }
                sendMessage(sender, getPrefixMessage(msg));
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }
}
