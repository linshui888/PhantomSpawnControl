package com.github.alexqp.phantomspawncontrol.command;

import com.github.alexqp.commons.command.AlexCommand;
import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.commons.messages.Debugable;
import com.github.alexqp.commons.messages.MessageTranslator;
import com.github.alexqp.phantomspawncontrol.command.giantPhantoms.GiantPhantomsSubCmd;
import com.github.alexqp.phantomspawncontrol.command.lootTables.LootTablesSubCmd;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStatsContainer;
import com.github.alexqp.phantomspawncontrol.data.player.PlayerStatsContainer;
import com.github.alexqp.phantomspawncontrol.main.PhantomSpawnControl;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhantomCommand extends AlexCommand {

    public PhantomCommand(@NotNull PhantomSpawnControl plugin, @NotNull PlayerStatsContainer playerStatsContainer, @Nullable PhantomStatsContainer phantomStatsContainer) {
        super("phantomspawncontrol", plugin, ChatColor.AQUA);
        this.setPermission(this.getPermission() + ".cmd");

        ConfigChecker configChecker = new ConfigChecker(plugin);
        ConfigurationSection msgSection = configChecker.checkConfigSection(plugin.getConfig(), "messages.cmd", ConsoleErrorType.ERROR);
        if (msgSection != null) {
            String prefix = configChecker.checkString(msgSection, "prefix", ConsoleErrorType.WARN, "");
            assert prefix != null;
            if (!prefix.isEmpty())
                this.setPrefix(MessageTranslator.translateBukkitColorCodes(prefix));
            String noPermLine = configChecker.checkString(msgSection, "noPerm", ConsoleErrorType.WARN, "&4You do not have permission.");
            assert noPermLine != null;
            this.setNoPermissionLine(MessageTranslator.translateBukkitColorCodes(noPermLine));

            String creditLine = configChecker.checkString(msgSection, "credits", ConsoleErrorType.WARN, "Use /psc help for all available commands.");
            assert creditLine != null;
            this.addCreditLine(MessageTranslator.translateBukkitColorCodes(creditLine));

            String usagePrefix = configChecker.checkString(msgSection, "wrongCmdUsagePrefix", ConsoleErrorType.WARN, "&CUsage:");
            assert usagePrefix != null;
            this.setUsagePrefix(MessageTranslator.translateBukkitColorCodes(usagePrefix));

            BaseComponent[] noPlayerMsg = MessageTranslator.translateBukkitColorCodes(Objects.requireNonNull(configChecker.checkString(msgSection, "noPlayerMsg", ConsoleErrorType.WARN, "There is no such player.")));

            List<AlexSubCommand> subCmds = new ArrayList<>();
            subCmds.add(new ReloadSubCmd(this, plugin));
            subCmds.add(this.buildToggleCmd(configChecker, msgSection, playerStatsContainer, noPlayerMsg));
            subCmds.add(this.buildPlayerStatsCmd(configChecker, msgSection, playerStatsContainer, noPlayerMsg));
            if (phantomStatsContainer != null) {
                subCmds.add(new GiantPhantomsSubCmd(this, plugin, phantomStatsContainer));
                subCmds.add(new LootTablesSubCmd(this, plugin, phantomStatsContainer));
            }

            StringBuilder debug = new StringBuilder("added subCmds: ");
            for (int i = 0; i + 1 < subCmds.size(); i++) {
                debug.append(subCmds.get(i).getName()).append(", ");
            }
            debug.append(subCmds.get(subCmds.size() - 1).getName());
            ConsoleMessage.debug(this.getClass(), (Debugable) plugin, debug.toString());

            this.addSubCmds(subCmds);
        }
    }

    private ToggleSubCmd buildToggleCmd(@NotNull ConfigChecker configChecker, @NotNull final ConfigurationSection msgSection,
                                        @NotNull PlayerStatsContainer playerStatsContainer, @NotNull BaseComponent[] noPlayerMsg) {
        ConfigurationSection section = configChecker.checkConfigSection(msgSection, "toggle", ConsoleErrorType.ERROR);
        String help = "toggles phantom spawning";
        String enable = "&2Phantom spawning was activated for %player%.";
        String disable = "&4Phantom spawning was deactivated for %player%.";
        if (section != null) {
            help = configChecker.checkString(section, "help", ConsoleErrorType.WARN, help);
            enable = configChecker.checkString(section, "enable", ConsoleErrorType.WARN, enable);
            disable = configChecker.checkString(section, "disable", ConsoleErrorType.WARN, disable);
        }
        assert enable != null && disable != null;
        return new ToggleSubCmd(new TextComponent(help), this, enable, disable, noPlayerMsg, playerStatsContainer);
    }

    private PlayerStatsCmd buildPlayerStatsCmd(@NotNull ConfigChecker configChecker, @NotNull final ConfigurationSection msgSection,
                                               @NotNull PlayerStatsContainer playerStatsContainer, @NotNull BaseComponent[] noPlayerMsg) {
        ConfigurationSection section = configChecker.checkConfigSection(msgSection, "playerstats", ConsoleErrorType.ERROR);
        String help = "shows the playerstats of a specific player";
        if (section != null) {
            help = configChecker.checkString(section, "help", ConsoleErrorType.WARN, help);
        }
        return new PlayerStatsCmd(new TextComponent(help), this, noPlayerMsg, playerStatsContainer);
    }
}
