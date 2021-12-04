package com.github.alexqp.phantomspawncontrol.command.giantPhantoms.SetSubCmd.LootTableSubCmd;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import com.github.alexqp.phantomspawncontrol.command.giantPhantoms.SetSubCmd.SetSubCmd;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class LootTableSubCmd extends AsyncContainerSubCmd {

    public LootTableSubCmd(@NotNull SetSubCmd parent) {
        super("lootTable", new TextComponent("sets a giant_phantom's lootTable"), parent);
        HashSet<AlexSubCommand> subCmds = new HashSet<>();
        subCmds.add(new AddSubCmd(this));
        subCmds.add(new RemoveSubCmd(this));
        this.addSubCmds(subCmds);
    }
}
