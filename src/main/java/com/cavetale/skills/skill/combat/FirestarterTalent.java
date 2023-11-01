package com.cavetale.skills.skill.combat;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Damageable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.util.Mobs.isFireImmune;

public final class FirestarterTalent extends Talent {
    protected final int radius = 5;
    protected final int ticksPerLevel = 80;

    protected FirestarterTalent() {
        super(TalentType.FIRESTARTER);
    }

    @Override
    public String getDisplayName() {
        return "Firestarter";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Set mobs on fire in an area",
                       "Melee hits that set mobs on fire will spread the fire"
                       + " in a " + radius + " block area around the target."
                       + " Requires an axe or sword enchanted with Fire Aspect.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.FLINT_AND_STEEL);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (item == null || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
            || !(MaterialTags.AXES.isTagged(item.getType()) || MaterialTags.SWORDS.isTagged(item.getType()))) return;
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " immune to fire: " + ((isFireImmune(mob)) ? "true" : "false"));
        }
        if (isFireImmune(mob)) return;
        int enchantmentLevel = item.getEnchantmentLevel(Enchantment.FIRE_ASPECT);
        if (enchantmentLevel <= 0) return;
        List<Damageable> firestarterTargets = new ArrayList<>();
        for (Damageable target : player.getWorld().getNearbyLivingEntities(mob.getLocation(), radius)) {
            if (target instanceof Monster && target.isValid() && player.hasLineOfSight(target)) {
                firestarterTargets.add(target);
            }
        }
        firestarterTargets.remove(mob);
        int totalTicks = enchantmentLevel * ticksPerLevel;
        for (Damageable target : firestarterTargets) {
        	if (target.getFireTicks() < totalTicks) {
        	    target.setFireTicks(totalTicks);
        	    // aggro passive mobs (spiders)
        	    EntityDamageByEntityEvent edbee = new EntityDamageByEntityEvent(player, target, EntityDamageEvent.DamageCause.FIRE_TICK, 1);
        	    target.damage(1, player);
                target.setLastDamageCause(edbee);
            }
        }
        return;
    }
};
