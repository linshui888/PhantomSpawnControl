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

package com.github.alexqp.phantomspawncontrol.command.giantPhantoms.SetSubCmd;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import com.github.alexqp.phantomspawncontrol.command.giantPhantoms.GiantPhantomsSubCmd;
import com.github.alexqp.phantomspawncontrol.command.giantPhantoms.SetSubCmd.LootTableSubCmd.LootTableSubCmd;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStatsContainer;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

public class SetSubCmd extends AsyncContainerSubCmd implements Observer {

    public SetSubCmd(@NotNull GiantPhantomsSubCmd parent) {
        super("set", new TextComponent("sets a giant_phantom's stat"), parent);
        this.addExtraArgument("score", GiantPhantomsSubCmd.translateIntegerToString(this.container.getDefinedScores()));
        HashSet<AlexSubCommand> subCmds = new HashSet<>();
        subCmds.add(new StatsSubCmd(this));
        subCmds.add(new LootTableSubCmd(this));
        this.addSubCmds(subCmds);

        // CMD IDEE: /.. set lootTable
        // CMD IDEE /.. set stats <option> <value>
        // GGF IN LIST INTEGRIEREN CLICKDINGS: /.. set stats <value1> <value2>
        // richtig nice wärs natürlich, wenn man im List cmd nen button summon und nen button edit hat
    }

    @Override
    public void makeFinal() throws IllegalStateException {
        this.internalMakeFinal();
        container.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof PhantomStatsContainer) {
            this.editExtraArgumentOption(0, GiantPhantomsSubCmd.translateIntegerToString(((PhantomStatsContainer) o).getDefinedScores()));
            ConsoleMessage.debug(this.getClass(), plugin, "updated giant_phantom scores");
        }
    }
}
