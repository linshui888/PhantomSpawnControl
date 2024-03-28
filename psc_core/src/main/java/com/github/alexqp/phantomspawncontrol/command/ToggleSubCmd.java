package com.github.alexqp.phantomspawncontrol.command;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.commons.messages.MessageTranslator;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToggleSubCmd extends AlexSubCommand {

    private final String toggleOtherPermission;

    private final String enable;
    private final String disable;
    private final BaseComponent noPlayerMsg;

    private final PlayerStatsContainer container;

    public static String hex(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, ChatColor.of(color) + "");
            matcher = pattern.matcher(message);
        }
        return message;
    }

    ToggleSubCmd(@NotNull TextComponent helpLine, @NotNull AlexSubCommand parent,
                 @NotNull String enable, @NotNull String disable, @NotNull BaseComponent[] noPlayerMsg,
                 @NotNull PlayerStatsContainer container) {
        super("toggle", helpLine, parent);
        this.toggleOtherPermission = this.getPermission() + ".other";
        this.enable = enable;
        this.disable = disable;
        this.noPlayerMsg = this.getPrefixMessage(noPlayerMsg);
        this.container = container;
        this.setCmdParamLine(new TextComponent("[player]"));
        this.makeFinal();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label,
                           @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments,
                           @NotNull String[] args, int startIndex) {
        assert this.getNoPermissionLine() != null;
        Player p;
        if (startIndex < args.length) {
            p = Bukkit.getPlayerExact(args[startIndex]);
            if (p == null) {
                sendMessage(sender, noPlayerMsg);
                return false;
            }
        } else {
            if (sender instanceof Player) {
                p = (Player) sender;
            } else {
                sendMessage(sender, this.getNoPermissionLine());
                return true;
            }
        }

        if (!sender.equals(p) && !sender.hasPermission(this.toggleOtherPermission)) {
            sendMessage(sender, this.getNoPermissionLine());
            return true;
        }

        if (container.getPlayerStats(p.getUniqueId()).toggleAllowPhantomSpawn())
            sendMessage(sender, this.getPrefixMessage(new ComponentBuilder().append(MessageTranslator.translateBukkitColorCodes(hex(enable.replace("%player%", p.getName())))).create()));
        else
            sendMessage(sender, this.getPrefixMessage(new ComponentBuilder().append(MessageTranslator.translateBukkitColorCodes(hex(disable.replace("%player%", p.getName())))).create()));
        return true;
    }

    @Override
    protected @NotNull List<String> additionalTabCompleterOptions(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        ArrayList<String> completions = new ArrayList<>();
        if (sender.hasPermission(this.toggleOtherPermission)) {
            for (Player p : Bukkit.getOnlinePlayers())
                completions.add(p.getName());
        }
        return completions;
    }
}
