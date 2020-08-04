package com.github.alexqp.phantomspawncontrol.command;

import com.github.alexqp.commons.command.better.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.data.player.PlayerStatsContainer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ToggleSubCmd extends AlexSubCommand {

    private BaseComponent enable;
    private BaseComponent disable;

    private PlayerStatsContainer container;

    ToggleSubCmd(@NotNull TextComponent helpLine, @NotNull AlexSubCommand parent, @NotNull BaseComponent[] enable, @NotNull BaseComponent[] disable, @NotNull PlayerStatsContainer container) {
        super("toggle", helpLine, parent);
        this.setIsConsoleCmd(false);
        this.enable = this.getPrefixMessage(enable);
        this.disable = this.getPrefixMessage(disable);
        this.container = container;
        this.makeFinal();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label,
                           @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments,
                           @NotNull String[] args, int startIndex) {
        if (container.getPlayerStats(((Player) sender).getUniqueId()).toggleAllowPhantomSpawn())
            sendMessage(sender, enable);
        else
            sendMessage(sender, disable);
        return true;

    }
}
