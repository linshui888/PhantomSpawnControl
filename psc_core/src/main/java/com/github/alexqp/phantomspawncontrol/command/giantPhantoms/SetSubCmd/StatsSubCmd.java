package com.github.alexqp.phantomspawncontrol.command.giantPhantoms.SetSubCmd;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStat;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStats;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class StatsSubCmd extends AsyncContainerSubCmd {

    private BaseComponent invalidOption = new TextComponent(new ComponentBuilder("There is no such option.").color(ChatColor.RED).create());
    private BaseComponent invalidValue = new TextComponent(new ComponentBuilder("Value invalid for the given option.").color(ChatColor.RED).create());
    private BaseComponent success = new TextComponent(new ComponentBuilder("Value of giant_phantom was successfully changed.").color(ChatColor.DARK_GREEN).create());

    StatsSubCmd(@NotNull SetSubCmd parent) {
        super("stats", new TextComponent("sets a giant_phantom's stat"), parent);
        this.setCmdParamLine(new TextComponent("<option> <value>"));
        this.makeFinal();
    }

    @Override
    public void makeFinal() throws IllegalStateException {
        this.internalMakeFinal();
        invalidOption = this.getPrefixMessage(invalidOption);
        invalidValue = this.getPrefixMessage(invalidValue);
        success = this.getPrefixMessage(success);
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label,
                              @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments,
                              @NotNull String[] args, int startIndex) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (args.length > startIndex + 1) {
                        String score = previousExtraArguments.get(previousExtraArguments.size() - 1);
                        PhantomStats stats = container.getPhantomStats(Integer.parseInt(score));
                        if (stats != null) {

                            PhantomStat<?> option = null;
                            for (PhantomStat<?> stat : PhantomStat.values()) {
                                if (stat.getConfigName().equalsIgnoreCase(args[startIndex])) {
                                    option = stat;
                                    break;
                                }
                            }

                            if (option == null) {
                                sendMessage(sender, invalidOption);
                            } else {
                                if (stats.setByOptionAsync(option, args[startIndex + 1])) {
                                    sendMessage(sender, success);
                                    return;
                                } else {
                                    sendMessage(sender, invalidValue);
                                }
                            }

                        } else {
                            sendMessage(sender, getPrefixMessage(new ComponentBuilder("Internal error. Please contact server administration.").color(ChatColor.DARK_RED).create()));
                            ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, "Please contact developer with error: " + score + " was no valid phantomStat but statsCommand proceeded.");
                            return;
                        }
                    }
                }
                catch (NumberFormatException ignored) {}
                sendMessage(sender, getUsageLine(label));
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    @Override
    protected @NotNull List<String> additionalTabCompleterOptions(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        List<String> list = new ArrayList<>();
        for (PhantomStat<?> stat : PhantomStat.values()) {
            list.add(stat.getConfigName());
        }
        return list;
    }

    @Override
    protected @NotNull List<String> getTabCompletion(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        List<String> completions = new ArrayList<>();
        if (args.length == startIndex + 1 && args[startIndex - 1].equals(PhantomStat.NAME.getConfigName())) {
            List<String> list = new ArrayList<>();
            list.add("reset");
            StringUtil.copyPartialMatches(args[startIndex], list, completions);
        }
        Collections.sort(completions);
        return completions;
    }
}
