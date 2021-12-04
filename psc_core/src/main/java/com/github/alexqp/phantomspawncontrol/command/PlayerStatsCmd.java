package com.github.alexqp.phantomspawncontrol.command;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.data.player.PlayerStatsContainer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatsCmd extends AlexSubCommand {

    private final PlayerStatsContainer container;
    private final BaseComponent noPlayerMsg;

    PlayerStatsCmd(@NotNull TextComponent helpLine, @NotNull AlexSubCommand parent, @NotNull BaseComponent[] noPlayerMsg, @NotNull PlayerStatsContainer container) {
        super("playerstats", helpLine, parent);
        this.container = container;
        this.noPlayerMsg = this.getPrefixMessage(noPlayerMsg);
        this.setCmdParamLine(new TextComponent("<player>"));
        this.makeFinal();
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        assert this.getNoPermissionLine() != null;
        if (startIndex < args.length) {
            Player p = Bukkit.getPlayerExact(args[startIndex]);
            if (p == null) {
                sendMessage(sender, noPlayerMsg);
                return false;
            }
            BaseComponent[] msg = new ComponentBuilder("PlayerStats of " + p.getName() + ": ").color(ChatColor.GOLD).append(container.getPlayerStats(p.getUniqueId()).toString()).reset().create();
            sendMessage(sender, this.getPrefixMessage(msg));
        }
        return true;
    }

    @Override
    protected @NotNull List<String> additionalTabCompleterOptions(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        ArrayList<String> completions = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers())
            completions.add(p.getName());
        return completions;
    }
}
