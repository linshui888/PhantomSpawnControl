package com.github.alexqp.phantomspawncontrol.main;

import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStat;
import org.bukkit.ChatColor;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Phantom;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import com.github.alexqp.phantomspawncontrol.data.phantom.PhantomStats;

import java.util.Objects;

public class InternalsProvider {


    protected String version = "core";

    @SuppressWarnings("WeakerAccess")
    public InternalsProvider() {
    }

    public void applyPhantomStats(@NotNull PhantomStats phantomStats, @NotNull Phantom phantom) throws InternalsError {

        try {
            assert PhantomStat.HEALTH.getAttribute() != null;
            assert PhantomStat.SPEED.getAttribute() != null;
            assert PhantomStat.RANGE.getAttribute() != null;
            assert PhantomStat.DAMAGE.getAttribute() != null;

            AttributeInstance healthAttr = Objects.requireNonNull(phantom.getAttribute(PhantomStat.HEALTH.getAttribute()), "phantom.getAttribute(HEALTH) is null");
            AttributeInstance speedAttr = Objects.requireNonNull(phantom.getAttribute(PhantomStat.SPEED.getAttribute()), "phantom.getAttribute(SPEED) is null");
            AttributeInstance rangeAttr = Objects.requireNonNull(phantom.getAttribute(PhantomStat.RANGE.getAttribute()), "phantom.getAttribute(RANGE) is null");
            AttributeInstance dmgAttr = Objects.requireNonNull(phantom.getAttribute(PhantomStat.DAMAGE.getAttribute()), "phantom.getAttribute(DAMAGE) is null");

            if (phantomStats.getName() != null)
                phantom.setCustomName(ChatColor.translateAlternateColorCodes('&', phantomStats.getName()));
            phantom.setSize(phantomStats.getSize());
            healthAttr.addModifier(this.getAttributeModifier(healthAttr.getBaseValue(), phantomStats.getHealth()));
            phantom.setHealth(phantomStats.getHealth());
            speedAttr.addModifier(this.getAttributeModifier(speedAttr.getBaseValue(), phantomStats.getSpeed()));
            rangeAttr.addModifier(this.getAttributeModifier(rangeAttr.getBaseValue(), phantomStats.getRange()));
            dmgAttr.addModifier(this.getAttributeModifier(dmgAttr.getBaseValue(), phantomStats.getAttackDmg()));

        }
        catch (NullPointerException e) {
            throw new InternalsError(e.getMessage(), this.version);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected AttributeModifier getAttributeModifier(double baseValue, double goalValue) {
        double modifyValue = goalValue - baseValue;
        return new AttributeModifier("PSC_Plugin", modifyValue, AttributeModifier.Operation.ADD_NUMBER);

    }
}
