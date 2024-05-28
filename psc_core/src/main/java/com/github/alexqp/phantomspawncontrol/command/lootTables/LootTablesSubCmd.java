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
import com.github.alexqp.phantomspawncontrol.command.lootTables.EditSubCmd.EditSubCmd;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStatsContainer;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class LootTablesSubCmd extends AsyncContainerSubCmd {

    public LootTablesSubCmd(@NotNull AlexSubCommand parent, @NotNull JavaPlugin plugin, @NotNull PhantomStatsContainer container) {
        super("lootTables", new TextComponent("All lootTable commands"), parent, plugin, container);
        HashSet<AlexSubCommand> subCmds = new HashSet<>();
        subCmds.add(new ListSubCmd(this));
        subCmds.add(new AddSubCmd(this));
        subCmds.add(new RemoveSubCmd(this));
        subCmds.add(new PopulateSubCmd(this));
        subCmds.add(new EditSubCmd(this));
        this.addSubCmds(subCmds);
    }
}
