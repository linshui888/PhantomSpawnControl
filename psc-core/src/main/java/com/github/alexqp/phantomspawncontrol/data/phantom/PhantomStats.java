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

package com.github.alexqp.phantomspawncontrol.data.phantom;

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConfigurationSerializableCheckable;
import com.github.alexqp.commons.config.ConsoleErrorType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// should be thread safe
public class PhantomStats implements ConfigurationSerializableCheckable {

    private static final String[] configNames = {"lootTable"};

    public static String getLootTablesConfigName() {
        return configNames[0];
    }

    private String name;
    private int size;
    private double health;
    private double speed;
    private double range;
    private double attackDmg;

    private ConcurrentHashMap<String, Integer> lootTables = new ConcurrentHashMap<>();

    @SuppressWarnings("ConstantConditions")
    public PhantomStats() {
        name = PhantomStat.NAME.getDefValue();
        size = PhantomStat.SIZE.getDefValue();
        health = PhantomStat.HEALTH.getDefValue();
        speed = PhantomStat.SPEED.getDefValue();
        range = PhantomStat.RANGE.getDefValue();
        attackDmg = PhantomStat.DAMAGE.getDefValue();
    }

    public PhantomStats(int size, double health, double speed, double range, double attackDmg) {
        this.size = size;
        this.health = health;
        this.speed = speed;
        this.range = range;
        this.attackDmg = attackDmg;
    }

    public PhantomStats(@Nullable String name, int size, double health, double speed, double range, double attackDmg) {
        this.name = name;
        this.size = size;
        this.health = health;
        this.speed = speed;
        this.range = range;
        this.attackDmg = attackDmg;
    }

    // only via PhantomStatsContainer
    PhantomStats(@Nullable String name, int size, double health, double speed, double range, double attackDmg, final Map<String, Integer> lootTables) {
        this(name, size, health, speed, range, attackDmg);
        this.lootTables = new ConcurrentHashMap<>(lootTables);
    }

    public PhantomStats copy() {
        return new PhantomStats(name, size, health, speed, range, attackDmg, lootTables);
    }

    @Nullable
    public String getName() {
        return name;
    }
    public int getSize() {
        return size;
    }

    public double getHealth() {
        return health;
    }
    public double getSpeed() {
        return speed;
    }
    public double getRange() {
        return range;
    }
    public double getAttackDmg() {
        return attackDmg;
    }

    private boolean setName(@Nullable String name) {
        if (name == null || PhantomStat.NAME.getAllowedRange().contains(name)) {
            this.name = name;
            return true;
        }
        return false;
    }
    private boolean setSize(int size) {
        if (PhantomStat.SIZE.getAllowedRange().contains(size)) {
            this.size = size;
            return true;
        }
        return false;
    }
    private boolean setHealth(double health) {
        if (PhantomStat.HEALTH.getAllowedRange().contains(health)) {
            this.health = health;
            return true;
        }
        return false;
    }
    private boolean setSpeed(double speed) {
        if (PhantomStat.SPEED.getAllowedRange().contains(speed)) {
            this.speed = speed;
            return true;
        }
        return false;
    }
    private boolean setRange(double range) {
        if (PhantomStat.RANGE.getAllowedRange().contains(range)) {
            this.range = range;
            return true;
        }
        return false;
    }
    private boolean setAttackDmg(double damage) {
        if (PhantomStat.RANGE.getAllowedRange().contains(damage)) {
            this.attackDmg = damage;
            return true;
        }
        return false;
    }

    public boolean setByOptionAsync(PhantomStat<?> option, String value) {
        synchronized(this) {
            try {
                if (option.equals(PhantomStat.NAME)) {
                    if (value.equals("reset"))
                        return this.setName(null);
                    return this.setName(value);
                }
                else if (option.equals(PhantomStat.SIZE)) {
                    return this.setSize(Integer.parseInt(value));
                }
                else if(option.equals(PhantomStat.HEALTH)) {
                    return this.setHealth(Double.parseDouble(value));
                }
                else if(option.equals(PhantomStat.SPEED)) {
                    return this.setSpeed(Double.parseDouble(value));
                }
                else if (option.equals(PhantomStat.RANGE)) {
                    return this.setRange(Double.parseDouble(value));
                }
                else if (option.equals(PhantomStat.DAMAGE)) {
                    return this.setAttackDmg(Double.parseDouble(value));
                }
            }
            catch(NumberFormatException e) {
                return false;
            }
            return false;
        }
    }

    public Set<String> getLootTables() {
        return lootTables.keySet();
    }

    private void addLootTableKeyAsync(@NotNull String name, int weight) {
        lootTables.put(name, weight);
    }

    // only via PhantomStatsContainer
    boolean addLootTableKeyAsync(@NotNull String name, int weight, @NotNull Collection<String> availableLootTables) {
        if (availableLootTables.contains(name)) {
            this.addLootTableKeyAsync(name, weight);
            return true;
        }
        return false;
    }

    public boolean removeLootTableKeyAsync(String name) {
        return lootTables.remove(name) != null;
    }

    @NotNull // thread safe
    String getLootTableKeyAsync(@NotNull Random random) {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (String lootTable : lootTables.keySet()) {
            int lastKey = map.isEmpty() ? 0 : map.lastKey();
            map.put(lootTables.get(lootTable) + lastKey, lootTable);
        }
        return Objects.toString(map.isEmpty() ? null : map.get(map.ceilingKey((random.nextInt(map.lastKey() + 1)))), "");
    }

    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(PhantomStat.NAME.getConfigName(), name);
        map.put(PhantomStat.SIZE.getConfigName(), size);
        map.put(PhantomStat.HEALTH.getConfigName(), health);
        map.put(PhantomStat.SPEED.getConfigName(), speed);
        map.put(PhantomStat.RANGE.getConfigName(), range);
        map.put(PhantomStat.DAMAGE.getConfigName(), attackDmg);
        map.put(configNames[0], this.lootTables);
        return map;
    }

    @SuppressWarnings({"unused", "unchecked"})
    public static PhantomStats deserialize(Map<String, Object> map) {
        try {
            return new PhantomStats(
                    (String) map.getOrDefault(PhantomStat.NAME.getConfigName(), PhantomStat.NAME.getDefValue()),
                    (int) map.getOrDefault(PhantomStat.SIZE.getConfigName(), PhantomStat.SIZE.getDefValue()),
                    (double) map.getOrDefault(PhantomStat.HEALTH.getConfigName(), PhantomStat.HEALTH.getDefValue()),
                    (double) map.getOrDefault(PhantomStat.SPEED.getConfigName(), PhantomStat.SPEED.getDefValue()),
                    (double) map.getOrDefault(PhantomStat.RANGE.getConfigName(), PhantomStat.RANGE.getDefValue()),
                    (double) map.getOrDefault(PhantomStat.DAMAGE.getConfigName(), PhantomStat.DAMAGE.getDefValue()),
                    (Map<String, Integer>) map.getOrDefault(configNames[0], new HashMap<>()));
        }
        catch (ClassCastException e) {
            return null;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean checkValues(ConfigChecker configChecker, ConfigurationSection section, String path, ConsoleErrorType errorType, boolean overwriteValues) {
        String sectionPath = section.getCurrentPath() + "." + path;

        int size = configChecker.checkValue(this.size, sectionPath, PhantomStat.SIZE.getConfigName(), errorType, PhantomStat.SIZE.getDefValue(), PhantomStat.SIZE.getAllowedRange());
        double health = configChecker.checkValue(this.health, sectionPath, PhantomStat.HEALTH.getConfigName(), errorType, PhantomStat.HEALTH.getDefValue(), PhantomStat.HEALTH.getAllowedRange());
        double speed = configChecker.checkValue(this.speed, sectionPath, PhantomStat.SPEED.getConfigName(), errorType, PhantomStat.SPEED.getDefValue(), PhantomStat.SPEED.getAllowedRange());
        double range = configChecker.checkValue(this.range, sectionPath, PhantomStat.RANGE.getConfigName(), errorType, PhantomStat.RANGE.getDefValue(), PhantomStat.RANGE.getAllowedRange());
        double attackDmg = configChecker.checkValue(this.attackDmg, sectionPath, PhantomStat.DAMAGE.getConfigName(), errorType, PhantomStat.DAMAGE.getDefValue(), PhantomStat.DAMAGE.getAllowedRange());

        if (overwriteValues) {
            this.size = size;
            this.health = health;
            this.speed = speed;
            this.range = range;
            this.attackDmg = attackDmg;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PhantomStats: "
                + PhantomStat.NAME.getConfigName() + " = " + this.name
                + ", " + PhantomStat.SIZE.getConfigName() + " = " + this.size
                + ", " + PhantomStat.HEALTH.getConfigName() + " = " + this.health
                + ", " + PhantomStat.SPEED.getConfigName() + " = " + this.speed
                + ", " + PhantomStat.RANGE.getConfigName() + " = " + this.range
                + ", " + PhantomStat.DAMAGE.getConfigName() + " = " + this.attackDmg
                + ", " + configNames[0] + " = " + this.lootTables;
    }
}
