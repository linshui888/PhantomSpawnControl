package com.github.alexqp.phantomspawncontrol.command.giantPhantoms;

import com.github.alexqp.commons.command.better.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class ListSubCmd extends AsyncContainerSubCmd {

    private BaseComponent header = new TextComponent("List of all giant_phantoms:");

    ListSubCmd(@NotNull GiantPhantomsSubCmd parent) {
        super("list", new TextComponent("lists all giant_phantoms"), parent);
        this.makeFinal();
    }

    @Override
    public void makeFinal() {
        this.internalMakeFinal();
        this.header = this.getPrefixMessage(header);
    }

    private BaseComponent getScoreLine(int score) {
        return new TextComponent(new ComponentBuilder().color(ChatColor.DARK_RED).append("Score = " + score + ": ").append(Objects.requireNonNull(container.getPhantomStats(score)).toString()).reset().create());
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label,
                              @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments,
                              @NotNull String[] args, int startIndex) {
        new BukkitRunnable() {

            @Override
            public void run() {
                sendMessage(sender, header);
                Set<Integer> scores = container.getDefinedScores();
                for (int score : scores) {
                    sendMessage(sender, getScoreLine(score));
                }
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }
}
