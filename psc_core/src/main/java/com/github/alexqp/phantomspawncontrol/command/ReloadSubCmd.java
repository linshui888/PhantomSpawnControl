package com.github.alexqp.phantomspawncontrol.command;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.main.PhantomSpawnControl;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReloadSubCmd extends AlexSubCommand {

    private PhantomSpawnControl plugin;
    private BaseComponent success;

    ReloadSubCmd(@NotNull AlexSubCommand parent, @NotNull PhantomSpawnControl plugin) {
        super("reload", new TextComponent("reloads the plugin"), parent);
        this.plugin = plugin;
        this.success = this.getPrefixMessage(new ComponentBuilder("Plugin reloaded.").color(ChatColor.DARK_GREEN).create());
        this.makeFinal();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label,
                           @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments,
                           @NotNull String[] args, int startIndex) {
        this.plugin.onReload();
        sendMessage(sender, success);
        return true;
    }
}
