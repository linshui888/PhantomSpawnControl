package com.github.alexqp.phantomspawncontrol.data.phantom.loottables;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.loot.LootContext;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class PhantomLootTableListener implements Listener {

    private final String metadataKey;

    public PhantomLootTableListener(@NotNull String metadataKey) {
        this.metadataKey = metadataKey;
    }

    @EventHandler
    public void onPhantomDeath(EntityDeathEvent e) {
        if (e.getEntityType().equals(EntityType.PHANTOM)) {
            Phantom phantom = (Phantom) e.getEntity();
            if (phantom.hasMetadata(metadataKey)) {
                for (MetadataValue metadataValue : phantom.getMetadata(metadataKey)) {
                    if (metadataValue.value() instanceof PhantomLootTable) {
                        LootContext.Builder lootContext = new LootContext.Builder(phantom.getLocation()).lootedEntity(phantom);

                        if (phantom.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                            Entity abuser = ((EntityDamageByEntityEvent) phantom.getLastDamageCause()).getDamager();
                            if (abuser instanceof HumanEntity) {
                                HumanEntity human = (HumanEntity) abuser;
                                lootContext.killer(human);
                                lootContext.lootingModifier(human.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS));
                                if (human.hasPotionEffect(PotionEffectType.LUCK)) {
                                    PotionEffect luck = human.getPotionEffect(PotionEffectType.LUCK);
                                    assert luck != null;
                                    lootContext.luck(luck.getAmplifier());
                                }
                            }
                        }

                        e.getDrops().clear();
                        e.getDrops().addAll(((PhantomLootTable) Objects.requireNonNull(metadataValue.value())).populateLoot(new Random(), lootContext.build()));
                    }
                }

            }
        }
    }
}
