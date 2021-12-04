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
