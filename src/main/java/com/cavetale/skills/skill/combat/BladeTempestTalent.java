package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
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
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Damageable;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static com.cavetale.skills.util.ItemCheck.isSword;
import static com.cavetale.skills.util.ItemCheck.getBaseAttackDamage;

public final class BladeTempestTalent extends Talent {
    protected static final double sweepBaseDamage = 0.375;
    protected static final double sweepDamagePerLevel = 0.125;
    protected static final int talentDuration = 4;
    protected static final double chargeMultiplier = 0.01; // 0.01 = +1% damage per charge
    protected double talentRadius = 12.0;
    protected int talentHitRate = 20;
    protected Duration DURATION = Duration.ofSeconds(talentDuration);
    protected static BukkitScheduler scheduler = Bukkit.getScheduler();
    //protected DamageCause sweep = EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK;

    protected BladeTempestTalent() {
        super(TalentType.BLADE_TEMPEST);
    }

    @Override
    public String getDisplayName() {
        return "Blade Tempest";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Sweeping enemies grants a buff that deals area damage over time",
                       "Requires a Sword!\n"
                       + "Blade Tempest refreshes its duration and gains a charge"
                       + " every time you sweep an enemy. The damage dealt by this talent"
                       + " is increased by +" + chargeMultiplier * 100 + "% damage per charge."
                       + " Having Sweeping Edge on your sword increases it by " + sweepDamagePerLevel / sweepBaseDamage * 100
                       + "% per level.",
                       "\nDamage: " + sweepBaseDamage * 100 + "% + "
                       + sweepDamagePerLevel * 100 + "% per Sweeping Edge level"
                       + " of the sword's unenchanted damage."
                       + "\nDuration: " + talentDuration + " second"
                       + "\nRange: " + talentRadius + " block"
                       + "\nHits every " + talentHitRate / 20 + " second"
                       + "\nDamage from this talent resets the invulnerability of mobs.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (!isSword(item)) return;
        Session session = sessionOf(player);
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " sourceless damage assigned to you!");
        }
        if (session.combat.isBladeTempestHitInProgress()) {
            event.setDamage(event.getDamage() * (1 + session.combat.getBladeTempestCharges() * chargeMultiplier));
        }
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (!isSword(item)) return;
        Session session = sessionOf(player);
        //if requirements valid & sweep damage & not from tempest
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;
        if (session.combat.getBladeTempestCooldown() > System.currentTimeMillis()) return; //on CD, nothing to do
        if (session.combat.getBladeTempestEndTime() < System.currentTimeMillis()) {
            //start new
            refreshBladeTempest(player);
            scheduleBladeTempestDamage(player);
            if (sessionOf(player).isDebugMode()) {
                player.sendMessage(talentType + " sched: first hit");
            }
            return;
        } else {
            refreshBladeTempest(player);
            if (session.combat.isBladeTempestExpiring()) {
                session.combat.getBladeTempestNextTask().cancel();
                session.combat.setBladeTempestExpiring(false);
                scheduleBladeTempestDamage(player);
            }
        }
    }

    protected void refreshBladeTempest(Player player) {
        Session session = sessionOf(player);
        session.combat.setBladeTempestCharges(session.combat.getBladeTempestCharges() + 1);
        session.combat.setBladeTempestEndTime(System.currentTimeMillis() + DURATION.toMillis());
    }

    //delay in ticks not seconds, elsewhere seconds are used
    //if lag is a concern, minor delays can be mitigated by increasing duration to 4.x or so, but that will occasionally result in +1 activation
    protected void scheduleBladeTempestDamage(Player player) {
        Session session = sessionOf(player);
        long delay = talentHitRate - Math.max(0, talentHitRate - ((System.currentTimeMillis() - session.combat.getBladeTempestLastTime()) / 50));
        BukkitTask bladeTempestActivation = scheduler.runTaskLater(skillsPlugin(), () -> {
            bladeTempestDamageTask(player);
        }, delay);
        session.combat.setBladeTempestNextTask(bladeTempestActivation);
    }

    protected static void bladeTempestExpireTask(Player player) {
        Session session = sessionOf(player);
        BukkitTask task = session.combat.getBladeTempestNextTask();
        session.combat.setBladeTempestNextTask(null);
        session.combat.setBladeTempestCharges(0);
        session.combat.setBladeTempestEndTime(0);
        session.combat.setBladeTempestHitInProgress(false); //this should not be needed
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage("Blade Tempest expired"); //talentType - non-static variable talentType cannot be referenced from a static context
        }
        if (task != null) task.cancel(); //can cancel itself, task shouldn't be null
    }

    protected void bladeTempestDamageTask(Player player) {
        if (!isPlayerEnabled(player)) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        //check player equipment, get base dmg (other talents apply on their own)
        if (!isSword(item)) return;
        // damage of the basic item
        double itemDamage = getBaseAttackDamage(item); //left for debug purposes
        // total damage from player; includes base attack damage and str(+3/lvl)/weakness(-4/lvl) (does not incl. ench)
        double attributeDamage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        int sweepLevel = item.getEnchantmentLevel(Enchantment.SWEEPING_EDGE);
        double sweepMultiplier = sweepBaseDamage + sweepDamagePerLevel * sweepLevel;
        double damage = attributeDamage * sweepMultiplier; //(itemDamage + attributeDamage) * sweepMultiplier; // attributeDamage already includes itemDamage
        //find targets
        List<Damageable> bladeTempestTargets = new ArrayList<>();
        for (Damageable target : player.getWorld().getNearbyLivingEntities(player.getLocation(), talentRadius)) {
            if (target instanceof Enemy && target.isValid() && player.hasLineOfSight(target)) {
                bladeTempestTargets.add(target);
            }
        }
        Session session = sessionOf(player);
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " attrDmg: " + attributeDamage + " itemDmg: " + itemDamage + "\nDmg w/o charges: " + damage + " Targets: " + bladeTempestTargets.size());
        }
        //lock sweeping
        session.combat.setBladeTempestHitInProgress(true);
        //deal damage to all targets, set dmg cause
        for (Damageable target : bladeTempestTargets) {
            ((LivingEntity)target).setNoDamageTicks(0);
            CombatSkill.combatTagMob((Mob) target, player);
            target.damage(damage, player);
            ((LivingEntity)target).setNoDamageTicks(0); //could save initial value and reset to that (BT deals damage w/o interruption)
        }
        //release sweeping
        session.combat.setBladeTempestHitInProgress(false);
        //log last time this run
        session.combat.setBladeTempestLastTime(System.currentTimeMillis());
        if (System.currentTimeMillis() > session.combat.getBladeTempestEndTime()) {
            //should already expire
            if (sessionOf(player).isDebugMode()) {
               player.sendMessage(talentType + " should've expired, expiring now");
            }
            BukkitTask bladeTempestExpire = scheduler.runTask(skillsPlugin(), () -> {
                bladeTempestExpireTask(player);
            });//why schedule and not just call the function?
            session.combat.setBladeTempestNextTask(bladeTempestExpire);
            session.combat.setBladeTempestExpiring(true);
            return;
        }
        if (session.combat.getBladeTempestEndTime() - System.currentTimeMillis() < 50 * talentHitRate) {
            //not enough time for another damage task
            if (sessionOf(player).isDebugMode()) {
                player.sendMessage(talentType + " sched: expiration");
            }
            long delay = (session.combat.getBladeTempestEndTime() - System.currentTimeMillis()) / 50;
            BukkitTask bladeTempestExpire = scheduler.runTaskLater(skillsPlugin(), () -> {
                bladeTempestExpireTask(player);
            }, delay);
            session.combat.setBladeTempestNextTask(bladeTempestExpire);
            session.combat.setBladeTempestExpiring(true);
            return;
        } else {
            //schedule next damage task
            if (sessionOf(player).isDebugMode()) {
                player.sendMessage(talentType + " sched: follow up");
            }
            long delay = talentHitRate - (System.currentTimeMillis() - session.combat.getBladeTempestLastTime()) / 50; // bit of lag resilience
            BukkitTask bladeTempestActivation = scheduler.runTaskLater(skillsPlugin(), () -> {
                bladeTempestDamageTask(player);
            }, delay);
            session.combat.setBladeTempestNextTask(bladeTempestActivation);
            return;
        }
    }
};
