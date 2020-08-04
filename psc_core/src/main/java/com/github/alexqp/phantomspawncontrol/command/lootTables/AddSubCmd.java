package com.github.alexqp.phantomspawncontrol.command.lootTables;

import com.github.alexqp.commons.command.better.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import com.github.alexqp.phantomspawncontrol.data.phantom.loottables.PhantomLootTable;
import com.github.alexqp.phantomspawncontrol.utility.ChestContent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class AddSubCmd extends AsyncContainerSubCmd {

    private BaseComponent noContainerError = new TextComponent(new ComponentBuilder("Please look at a chest!").color(ChatColor.RED).create());
    private BaseComponent existError = new TextComponent(new ComponentBuilder("There is already a lootTable with that name!").color(ChatColor.RED).create());
    private BaseComponent success = new TextComponent(new ComponentBuilder("LootTable was successfully added.").color(ChatColor.DARK_GREEN).create());

    AddSubCmd(@NotNull LootTablesSubCmd parent) {
        super("add", new TextComponent("adds a lootTable"), parent);
        this.setIsConsoleCmd(false);
        this.setCmdParamLine(new TextComponent("<name>"));
        this.makeFinal();
    }

    @Override
    public void makeFinal() {
        this.internalMakeFinal();
        this.noContainerError = this.getPrefixMessage(noContainerError);
        this.existError = this.getPrefixMessage(existError);
        this.success = this.getPrefixMessage(success);
    }

    @Override
    public @NotNull BaseComponent getUsageLine(@NotNull String label) throws IllegalStateException {
        BaseComponent usageLine = super.getUsageLine(label);
        usageLine.addExtra(". In addition you have to look at a chest!");
        return usageLine;
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        if (args.length > startIndex) {
            assert sender instanceof Player;
            Player p = (Player) sender;
            List<ItemStack> contents = ChestContent.getTargetContents(p);
            if (contents != null) {
                PhantomLootTable lootTable = new PhantomLootTable(new NamespacedKey(plugin, args[startIndex]), contents);
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (container.registerLootTableAsync(lootTable.getKey().getKey(), lootTable)) {
                            sendMessage(sender, success);
                        } else {
                            sendMessage(sender, existError);
                        }
                    }
                }.runTaskAsynchronously(plugin);
                return true;
            } else {
                sendMessage(sender, noContainerError);
            }
        }
        return false;
    }
}
