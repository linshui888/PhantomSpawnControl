/*
 * Copyright (C) 2018-2024 Alexander Schmid
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.alexqp.phantomspawncontrol.command.lootTables.EditSubCmd;

import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.phantomspawncontrol.command.AsyncContainerSubCmd;
import com.github.alexqp.phantomspawncontrol.utility.ChestContent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class LootSubCmd extends AsyncContainerSubCmd {

    private BaseComponent noContainerError = new TextComponent(new ComponentBuilder("Please look at a chest!").color(ChatColor.RED).create());
    private BaseComponent existError = new TextComponent(new ComponentBuilder("There is no lootTable with that name!").color(ChatColor.RED).create());
    private BaseComponent success = new TextComponent(new ComponentBuilder("LootTable's loot was successfully edited.").color(ChatColor.DARK_GREEN).create());


    LootSubCmd(@NotNull EditSubCmd parent) {
        super("loot", new TextComponent("edits a lootTable's loot"), parent);
        this.setPermission(parent.getPermission());
        this.setIsConsoleCmd(false);
        this.makeFinal();
    }

    @Override
    public void makeFinal() throws IllegalStateException {
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
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label,
                              @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments,
                              @NotNull String[] args, int startIndex) {
        if (args.length > startIndex) {
            assert sender instanceof Player;
            Player p = (Player) sender;
            List<ItemStack> contents = ChestContent.getTargetContents(p);
            if (contents != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (container.editLootOfLootTableAsync(args[startIndex], contents)) {
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
