package com.github.alexqp.phantomspawncontrol.command.lootTables;

import com.github.alexqp.commons.command.better.AlexSubCommand;
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
