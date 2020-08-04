package com.github.alexqp.phantomspawncontrol.command.giantPhantoms.SetSubCmd.LootTableSubCmd;

import com.github.alexqp.commons.command.better.AlexSubCommand;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStats;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class AddSubCmd extends AsyncContainerSubCmd {

    private BaseComponent notFound = new TextComponent(new ComponentBuilder("There is no such lootTable.").color(ChatColor.RED).create());
    private BaseComponent success = new TextComponent(new ComponentBuilder("Successfully added lootTable to giant_phantom.").color(ChatColor.DARK_GREEN).create());

    AddSubCmd(@NotNull AsyncContainerSubCmd parent) {
        super("add", new TextComponent("adds a lootTable to a giant_phantom"), parent);
        this.setCmdParamLine(new TextComponent("<lootTable> [<weight>]"));
        this.setPermission(parent.getPermission());
        this.makeFinal();
    }

    @Override
    public void makeFinal() throws IllegalStateException {
        this.internalMakeFinal();
        notFound = this.getPrefixMessage(notFound);
        success = this.getPrefixMessage(success);
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    if (args.length > startIndex) {
                        String score = previousExtraArguments.get(previousExtraArguments.size() - 1);
                        PhantomStats stats = container.getPhantomStats(Integer.parseInt(score));
                        if (stats != null) {
                            int weight = 1;
                            if (args.length > startIndex + 1) {
                                ConsoleMessage.debug(AddSubCmd.class, plugin, "Trying to parse weight...");
                                weight = Integer.parseInt(args[startIndex + 1]);
                            }
                            if (container.addLootTableToPhantomStatsAsync(Integer.parseInt(score), args[startIndex], weight)) {
                                sendMessage(sender, success);
                                return;
                            } else {
                                sendMessage(sender, notFound);
                            }
                        } else {
                            sendMessage(sender, getPrefixMessage(new ComponentBuilder("Internal error. Please contact server administration.").color(ChatColor.DARK_RED).create()));
                            ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, "Please contact developer with error: " + score + " was no valid phantomStat but statsCommand proceeded.");
                            return;
                        }
                    }
                } catch (NumberFormatException ignored) {}
                sendMessage(sender, getUsageLine(label));
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    @Override
    protected @NotNull List<String> additionalTabCompleterOptions(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        return new ArrayList<>(container.getRegisteredLootTables());
    }
}
