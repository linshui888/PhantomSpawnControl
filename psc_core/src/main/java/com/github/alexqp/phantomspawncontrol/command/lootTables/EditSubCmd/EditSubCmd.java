package com.github.alexqp.phantomspawncontrol.command.lootTables.EditSubCmd;

import com.github.alexqp.commons.command.better.AlexSubCommand;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import com.github.alexqp.phantomspawncontrol.command.lootTables.EditSubCmd.ChildrenSubCmd.ChildrenSubCmd;
import com.github.alexqp.phantomspawncontrol.command.lootTables.LootTablesSubCmd;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStatsContainer;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

public class EditSubCmd extends AsyncContainerSubCmd implements Observer {

    public EditSubCmd(@NotNull LootTablesSubCmd parent) {
        super("edit", new TextComponent("edits a lootTable"), parent);
        this.addExtraArgument("name", container.getRegisteredLootTables());
        HashSet<AlexSubCommand> subCmds = new HashSet<>();
        subCmds.add(new LootSubCmd(this));
        subCmds.add(new ChildrenSubCmd(this));
        this.addSubCmds(subCmds);
    }

    @Override
    public void makeFinal() throws IllegalStateException {
        this.internalMakeFinal();
        container.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof PhantomStatsContainer) {
            this.editExtraArgumentOption(0, container.getRegisteredLootTables());
            ConsoleMessage.debug(this.getClass(), plugin, "updated lootTable names");
        }
    }
}
