package com.github.alexqp.phantomspawncontrol.command.lootTables.EditSubCmd.ChildrenSubCmd;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class AddSubCmd extends AsyncContainerSubCmd {

    private BaseComponent success = new TextComponent(new ComponentBuilder("Successfully added child reference.").color(ChatColor.DARK_GREEN).create());
    private BaseComponent notFound = new TextComponent(new ComponentBuilder("There is no child with that name!").color(ChatColor.RED).create());

    AddSubCmd(@NotNull ChildrenSubCmd parent) {
        super("add", new TextComponent("adds a child reference to a lootTable"), parent);
        this.setPermission(parent.getPermission());
        this.setCmdParamLine(new TextComponent("<child>"));
        this.makeFinal();
    }

    @Override
    public void makeFinal() {
        this.internalMakeFinal();
        this.success = this.getPrefixMessage(success);
        this.notFound = this.getPrefixMessage(notFound);
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds,
                              @NotNull List<String> previousExtraArguments,
                              @NotNull String[] args, int startIndex) {
        if (args.length > startIndex) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    if (container.addChildReferenceToLootTableAsync(previousExtraArguments.get(previousExtraArguments.size() - 1), args[startIndex])) {
                        sendMessage(sender, success);
                    } else {
                        sendMessage(sender, notFound);
                    }
                }
            }.runTaskAsynchronously(plugin);
            return true;
        }
        return false;
    }

    @Override
    protected @NotNull List<String> additionalTabCompleterOptions(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        String parent = previousExtraArguments.get(previousExtraArguments.size() - 1);
        List<String> completions = new ArrayList<>(container.getRegisteredLootTables());
        Set<String> children = container.getChildReferencesOfLootTable(parent);
        if (children == null) {
            sendMessage(sender, getPrefixMessage(new ComponentBuilder("Internal error. Please contact server administration.").color(ChatColor.DARK_RED).create()));
            ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, "Please contact developer with error: " + parent + " was no valid lootTable but childrenCommand proceeded.");
        } else {
            completions.removeAll(children);
        }
        return completions;
    }
}
