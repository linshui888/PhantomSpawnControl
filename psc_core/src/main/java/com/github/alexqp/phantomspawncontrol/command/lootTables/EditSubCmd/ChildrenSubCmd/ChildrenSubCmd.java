package com.github.alexqp.phantomspawncontrol.command.lootTables.EditSubCmd.ChildrenSubCmd;

import com.github.alexqp.commons.command.better.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import com.github.alexqp.phantomspawncontrol.command.lootTables.EditSubCmd.EditSubCmd;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class ChildrenSubCmd extends AsyncContainerSubCmd {

    public ChildrenSubCmd(@NotNull EditSubCmd parent) {
        super("children", new TextComponent("edits a lootTable's children"), parent);
        this.setPermission(parent.getPermission());
        HashSet<AlexSubCommand> subCmds = new HashSet<>();
        subCmds.add(new AddSubCmd(this));
        subCmds.add(new RemoveSubCmd(this));
        this.addSubCmds(subCmds);
    }
}
