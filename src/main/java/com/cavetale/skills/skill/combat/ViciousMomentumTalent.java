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
    static int maxSpeedLevel = 1;
    static int speedDuration = 20;

    protected ViciousMomentumTalent() {
        super(TalentType.VICIOUS_MOMENTUM);
    }

    @Override
    public String getDisplayName() {
        return "Vicious Momentum";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Melee kills give you Swiftness",
                       "Kills with melee weapons grant Swiftness for "
                       + speedDuration + " seconds. If you already have Swiftness,"
                       + " you gain a higher level, up to Swiftness " + maxSpeedLevel);
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }

    protected void onMeleeKill(Player player, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        int speedLevel = -1;
        if (player.hasPotionEffect(PotionEffectType.SPEED)) speedLevel = Math.min(player.getPotionEffect(PotionEffectType.SPEED).getAmplifier(), maxSpeedLevel);
        speedLevel = (speedLevel < maxSpeedLevel) ? speedLevel + 1 : maxSpeedLevel;
        PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, speedDuration * 20, speedLevel, false, false, true);
        player.addPotionEffect(effect);
    }
};
