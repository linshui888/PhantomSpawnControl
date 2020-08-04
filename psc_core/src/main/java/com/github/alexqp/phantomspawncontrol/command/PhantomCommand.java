package com.github.alexqp.phantomspawncontrol.command;

import com.github.alexqp.commons.command.better.AlexCommand;
import com.github.alexqp.commons.command.better.AlexSubCommand;
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
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PhantomCommand extends AlexCommand {

    public PhantomCommand(@NotNull PhantomSpawnControl plugin, @NotNull PlayerStatsContainer playerStatsContainer, @Nullable PhantomStatsContainer phantomStatsContainer) {
        super("phantomspawncontrol", plugin, ChatColor.AQUA);
        this.setPermission(this.getPermission() + ".cmd");

        ConfigChecker configChecker = new ConfigChecker(plugin);
        ConfigurationSection msgSection = configChecker.checkConfigSection(plugin.getConfig(), "messages.cmd", ConsoleErrorType.ERROR);
        if (msgSection != null) {
            String noPermLine = configChecker.checkString(msgSection, "noPerm", ConsoleErrorType.WARN, "&4You do not have permission.");
            assert noPermLine != null;
            this.setNoPermissionLine(MessageTranslator.translateBukkitColorCodes(noPermLine));

            String creditLine = configChecker.checkString(msgSection, "credits", ConsoleErrorType.WARN, "Use /psc help for all available commands.");
            assert creditLine != null;
            this.addCreditLine(MessageTranslator.translateBukkitColorCodes(creditLine));

            String usagePrefix = configChecker.checkString(msgSection, "wrongCmdUsagePrefix", ConsoleErrorType.WARN, "&CUsage:");
            assert usagePrefix != null;
            this.setUsagePrefix(MessageTranslator.translateBukkitColorCodes(usagePrefix));

            List<AlexSubCommand> subCmds = new ArrayList<>();
            subCmds.add(new ReloadSubCmd(this, plugin));
            subCmds.add(this.buildToggleCmd(configChecker, msgSection, playerStatsContainer));
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

    private ToggleSubCmd buildToggleCmd(@NotNull ConfigChecker configChecker, @NotNull final ConfigurationSection msgSection, @NotNull PlayerStatsContainer playerStatsContainer) {
        ConfigurationSection section = configChecker.checkConfigSection(msgSection, "toggle", ConsoleErrorType.ERROR);
        String help = "toggles phantom spawning";
        String enable = "&2Phantom spawning was activated for you.";
        String disable = "&4Phantom spawning was deactivated for you.";
        if (section != null) {
            help = configChecker.checkString(section, "help", ConsoleErrorType.WARN, help);
            enable = configChecker.checkString(section, "enable", ConsoleErrorType.WARN, enable);
            disable = configChecker.checkString(section, "disable", ConsoleErrorType.WARN, disable);
        }
        assert enable != null;
        assert disable != null;
        return new ToggleSubCmd(new TextComponent(help), this, MessageTranslator.translateBukkitColorCodes(enable), MessageTranslator.translateBukkitColorCodes(disable), playerStatsContainer);
    }
}
