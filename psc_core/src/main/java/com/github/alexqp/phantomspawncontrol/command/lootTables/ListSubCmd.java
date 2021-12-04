package com.github.alexqp.phantomspawncontrol.command.lootTables;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class ListSubCmd extends AsyncContainerSubCmd {

    ListSubCmd(@NotNull LootTablesSubCmd parent) {
        super("list", new TextComponent("lists all lootTables"), parent);
        this.makeFinal();
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label,
                              @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments,
                              @NotNull String[] args, int startIndex) {
        new BukkitRunnable() {

            @Override
            public void run() {
                TextComponent msg = new TextComponent("LootTables: ");
                List<String> lootTables = new ArrayList<>(container.getRegisteredLootTables());
                // TODO we need abstractLootTables and children!
                for (int i = 0; i + 1 < lootTables.size(); i++) {
                    msg.addExtra(lootTables.get(i) + ", ");
                }
                if (lootTables.size() != 0) {
                    msg.addExtra(lootTables.get(lootTables.size() - 1));
                }
                sendMessage(sender, getPrefixMessage(msg));
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }
}
