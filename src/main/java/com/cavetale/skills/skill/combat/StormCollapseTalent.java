package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.skill.combat.BladeTempestTalent;
import com.cavetale.skills.util.ItemCheck;
import com.destroystokyo.paper.MaterialTags;
import java.lang.Math;
import java.util.List;
import java.util.ArrayList;
import java.time.Duration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Damageable;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static com.cavetale.skills.util.ItemCheck.isSword;
//import static com.cavetale.skills.skill.combat.BladeTempestTalent.bladeTempestExpireTask;

/*Storm Collapse
Activation/trigger: critical hit
Requirements: sword, Sweeping Edge enchantment, fully charged critical hit, having Blade Tempest stacks
Description: Collapse your Blade Tempest with a critical hit, consuming all of its charges to deal damage in a large area. You cannot gain Blade Tempest stacks for x seconds
18 block radius (1,5x Blade Tempest's range)
deals 0.5 damage per charges consumed (probably needs an upper cap for damage or charges to avoid one tapping bosses)
damage is affected by generic damage talents (Pyromaniac)
ignores and doesn't create invulnerability
disables activating Blade Tempest for x seconds*/

public final class StormCollapseTalent extends Talent {
    protected static final double damageMultiplier = 0.5;
    protected static final double talentRadius = 18;
    protected static final int disableDuration = 12;
    protected Duration DURATION = Duration.ofSeconds(disableDuration);

    protected StormCollapseTalent() {
        super(TalentType.STORM_COLLAPSE);
    }

    @Override
    public String getDisplayName() {
        return "Storm Collapse";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Collapse your Blade Tempest to deal damage in a large area",
                       "Disables Blade Tempest for " + disableDuration + " seconds"
                       + "\nRange: " + talentRadius
                       + "\nDamage: " + damageMultiplier + " x number of charges");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        //player.sendMessage("player valid");
        if (!isSword(item)) return;
        //player.sendMessage("item is sword");
        if (item.getEnchantmentLevel(Enchantment.SWEEPING_EDGE) == 0) return;
        //player.sendMessage("enchantment valid");
        player.sendMessage("attack: " + player.getAttackCooldown());
        if (!event.isCritical() /*|| player.getAttackCooldown() != 1.0*/) return;
        //player.sendMessage("crit and full attack");
        Session session = sessionOf(player);
        player.sendMessage("event dmg: " + event.getDamage());
        if (session.combat.getBladeTempestCharges() != 0) {
            int charges = session.combat.getBladeTempestCharges();
            player.sendMessage("charges: " + charges);
            BladeTempestTalent.bladeTempestExpireTask(player);
            session.combat.setBladeTempestCooldown(System.currentTimeMillis() + DURATION.toMillis());
            player.sendMessage("CD set, prep for damage");
            double damage = charges * damageMultiplier;
            List<Damageable> stormCollapseTargets = new ArrayList<>();
            for (Damageable target : player.getWorld().getNearbyLivingEntities(player.getLocation(), talentRadius)) {
                if (target instanceof Enemy && target.isValid() && player.hasLineOfSight(target)) {
                    stormCollapseTargets.add(target);
                }
            }
            if (sessionOf(player).isDebugMode()) {
                player.sendMessage(talentType + " Targets: " + stormCollapseTargets.size() + " Damage: " + damage);
            }
            for (Damageable target : stormCollapseTargets) {
                ((LivingEntity)target).setNoDamageTicks(0);
                target.damage(damage, player); //always entity_attack; fine here because triggered by an attack
                ((LivingEntity)target).setNoDamageTicks(0);
            }
        }
    }
}