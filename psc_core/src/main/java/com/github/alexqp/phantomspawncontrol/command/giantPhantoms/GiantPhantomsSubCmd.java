package com.github.alexqp.phantomspawncontrol.command.giantPhantoms;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import com.github.alexqp.phantomspawncontrol.command.giantPhantoms.SetSubCmd.SetSubCmd;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStatsContainer;
import com.github.alexqp.phantomspawncontrol.main.InternalsProvider;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GiantPhantomsSubCmd extends AsyncContainerSubCmd {

    public GiantPhantomsSubCmd(@NotNull AlexSubCommand parent, @NotNull JavaPlugin plugin, @NotNull InternalsProvider internals, @NotNull PhantomStatsContainer container) {
        super("giantPhantoms", new TextComponent("All giantPhantom commands"), parent, plugin, container);
        HashSet<AlexSubCommand> subCmds = new HashSet<>();
        subCmds.add(new AddSubCmd(this));
        subCmds.add(new ListSubCmd(this));
        subCmds.add(new RemoveSubCmd(this));
        subCmds.add(new SummonSubCmd(this, internals));
        subCmds.add(new SetSubCmd(this));
        this.addSubCmds(subCmds);
    }

    public static Set<String> translateIntegerToString(@NotNull Set<Integer> scores) {
        Set<String> stringScores = new HashSet<>();
        for (Integer score : scores) {
            stringScores.add(String.valueOf(score));
        }
        return stringScores;
    }
}
