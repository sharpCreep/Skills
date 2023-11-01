package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Damageable;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class HeadtakerTalent extends Talent {
    private static double radius = 4.0;

    protected HeadtakerTalent() {
        super(TalentType.HEADTAKER);
    }

    @Override
    public String getDisplayName() {
        return "Headtaker";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Deal damage around you when you kill a mob with an axe hit",
                       "Fully charged, non-critical axe attacks that kill a mob"
                       + " will deal your attack's damage in a " + (int) radius + " block wide area.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }
    
    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (item == null || !MaterialTags.AXES.isTagged(item.getType())
            || player.getAttackCooldown() != 1.0 || event.isCritical()
            || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
            || mob.getHealth() > event.getFinalDamage()) return;
        List<Damageable> headtakerTargets = new ArrayList<>();
        for (Damageable target : player.getWorld().getNearbyLivingEntities(player.getLocation(), radius)) {
            if (target instanceof Monster && target.isValid() && player.hasLineOfSight(target)) {
                headtakerTargets.add(target);
            }
        }
        headtakerTargets.remove(mob);
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " targets: " + headtakerTargets.size() + " damage: " + event.getDamage());
        }
        for (Damageable target : headtakerTargets) {
        	EntityDamageByEntityEvent edbee = new EntityDamageByEntityEvent(player, target, EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK, event.getDamage());
        	target.damage(event.getDamage()); //type specific damage bonuses are proliferated (Smite, BoA), on hit debuffs are not applied (Fire Aspect, BoA, Knockback)
            target.setLastDamageCause(edbee);
        }
        return;
    }
};
