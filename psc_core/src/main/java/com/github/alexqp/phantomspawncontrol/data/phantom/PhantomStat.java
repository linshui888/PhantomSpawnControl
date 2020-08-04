package com.github.alexqp.phantomspawncontrol.data.phantom;

import com.google.common.collect.Range;
import org.bukkit.attribute.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PhantomStat<T extends Comparable<?>> {

    public static final PhantomStat<String> NAME = new PhantomStat<>("name", Range.all(), null, null);
    public static final PhantomStat<Integer> SIZE = new PhantomStat<>("size", Range.atLeast(0), 0, null);
    public static final PhantomStat<Double> HEALTH = new PhantomStat<>("health", Range.greaterThan(0.0), 20.0, Attribute.GENERIC_MAX_HEALTH);
    public static final PhantomStat<Double> SPEED = new PhantomStat<>("speed", Range.all(), 0.7, Attribute.GENERIC_MOVEMENT_SPEED);
    public static final PhantomStat<Double> RANGE = new PhantomStat<>("follow_distance", Range.greaterThan(0.0), 16.0, Attribute.GENERIC_FOLLOW_RANGE);
    public static final PhantomStat<Double> DAMAGE = new PhantomStat<>("attack_damage", Range.atLeast(0.0), 6.0, Attribute.GENERIC_ATTACK_DAMAGE);

    private static final List<PhantomStat<?>> values = new ArrayList<PhantomStat<?>>() {{
       add(PhantomStat.NAME);
       add(PhantomStat.SIZE);
       add(PhantomStat.HEALTH);
       add(PhantomStat.SPEED);
       add(PhantomStat.RANGE);
       add(PhantomStat.DAMAGE);
       this.sort(Comparator.comparing(PhantomStat::getConfigName));
    }};

    @NotNull
    public static List<PhantomStat<?>> values() {
        return values;
    }

    private final String name;
    private final T defValue;
    private final Range<Comparable<?>> range;
    private final Attribute attribute;

    private PhantomStat(@NotNull String name, @NotNull Range<Comparable<?>> range, @Nullable T defValue, Attribute attribute) {
        this.name = name;
        this.defValue = defValue;
        this.range = range;
        this.attribute = attribute;
    }

    @NotNull
    public String getConfigName() {
        return name;
    }

    @Nullable
    T getDefValue() {
        return defValue;
    }

    @NotNull
    Range<Comparable<?>> getAllowedRange() {
        return range;
    }

    @Nullable
    public Attribute getAttribute() {
        return attribute;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PhantomStat) {
           return this.getConfigName().equals(((PhantomStat<?>) obj).getConfigName());
        }
        return false;
    }
}
