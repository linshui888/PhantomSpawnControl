package com.github.alexqp.phantomspawncontrol.command.lootTables;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import com.github.alexqp.phantomspawncontrol.data.phantom.loottables.PhantomLootTable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootContext;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class PopulateSubCmd extends AsyncContainerSubCmd {

    private BaseComponent success = new TextComponent(new ComponentBuilder("Successfully populated lootTable.").color(ChatColor.DARK_GREEN).create());
    private BaseComponent notFound = new TextComponent(new ComponentBuilder("There is no lootTable with that name!").color(ChatColor.RED).create());

    PopulateSubCmd(@NotNull LootTablesSubCmd parent) {
        super("populate", new TextComponent("populates a lootTable's loot including children"), parent);
        this.setIsConsoleCmd(false);
        this.setCmdParamLine(new TextComponent("<name>"));
        this.makeFinal();
    }

    @Override
    public void makeFinal() {
        this.internalMakeFinal();
        this.success = this.getPrefixMessage(success);
        this.notFound = this.getPrefixMessage(notFound);
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label,
                              @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments,
                              @NotNull String[] args, int startIndex) {

        new BukkitRunnable() {
            @Override
            public void run() {
                if (args.length > startIndex) {
                    PhantomLootTable lootTable = container.getLootTable(args[startIndex]);
                    if (lootTable != null) {
                        assert sender instanceof Player;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Player p = (Player) sender;
                                Inventory inv = Bukkit.createInventory(p, 54, " LootTable: " + lootTable.getKey().getKey());
                                lootTable.fillInventory(inv, new Random(), new LootContext.Builder(p.getLocation()).build());
                                p.openInventory(inv);
                                sendMessage(sender, success);
                            }
                        }.runTask(plugin);
                    } else {
                        sendMessage(sender, notFound);
                        sendMessage(sender, getUsageLine(label));
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    @Override
    protected @NotNull List<String> additionalTabCompleterOptions(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        return new ArrayList<>(container.getRegisteredLootTables());
    }
}
