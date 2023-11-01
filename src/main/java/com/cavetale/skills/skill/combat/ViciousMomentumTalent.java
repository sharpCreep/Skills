package com.cavetale.skills.skill.combat;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
import java.lang.Math;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class ViciousMomentumTalent extends Talent {
    static int maxSpeedLevel = 2;
    static int speedDuration = 5;

    protected ViciousMomentumTalent() {
        super(TalentType.VICIOUS_MOMENTUM);
    }

    @Override
    public String getDisplayName() {
        return "Vicious Momentum";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Melee hits give you Swiftness",
                       "Fully charged hits with melee weapons grant Swiftness for "
                       + speedDuration + " seconds. If you already have Swiftness,"
                       + " you gain a higher level, up to Swiftness " + maxSpeedLevel);
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (item == null || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
            || !(MaterialTags.AXES.isTagged(item.getType()) || MaterialTags.SWORDS.isTagged(item.getType()))
            || player.getAttackCooldown() != 1.0) return;
        int speedLevel = 0;
        if (player.hasPotionEffect(PotionEffectType.SPEED)) speedLevel = Math.min(player.getPotionEffect(PotionEffectType.SPEED).getAmplifier(), maxSpeedLevel);
        speedLevel = (speedLevel < maxSpeedLevel) ? speedLevel + 1 : maxSpeedLevel;
        PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, speedDuration * 20, speedLevel, false, false, true);
        player.addPotionEffect(effect);
    }
};
