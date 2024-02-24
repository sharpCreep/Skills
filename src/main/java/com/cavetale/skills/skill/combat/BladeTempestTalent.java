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
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class BladeTempestTalent extends Talent {
    protected static final double sweepBaseDamage = 0.375;
    protected static final double sweepDamagePerLevel = 0.125;
    protected static final int talentDuration = 4;
    protected static final double chargeMultiplier = 0.01; // 0.01 = +1% damage per charge
    protected double talentRadius = 12.0;
    protected Duration DURATION = Duration.ofSeconds(talentDuration);

    protected BladeTempestTalent() {
        super(TalentType.BLADE_TEMPEST);
    }

    @Override
    public String getDisplayName() {
        return "Blade Tempest";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Sweeping enemies grants a short buff that deals area damage when you sweep",
                       "Duration is refreshed and a charge is gained every time you sweep."
                       + " The damage is increased by Sweeping Edge and the number of charges."
                       + "\nDamage from this talent resets the invulnerability of mobs.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);
    }
    
    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (!isSword(item)) return;
        Session session = sessionOf(player);
        // BT attack, charges affect damage
        if (session.combat.isBladeTempestHit() && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            event.setDamage(event.getDamage() * (1 + session.combat.getBladeTempestCharges() * chargeMultiplier));
            return;
        }
        // Regular attack, sweeps activate talent
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;
        if (session.combat.getBladeTempestEndTime() < System.currentTimeMillis()) {
            session.combat.setBladeTempestCharges(1);
        }
        else {
            session.combat.setBladeTempestCharges(session.combat.getBladeTempestCharges() + 1);
        }
        session.combat.setBladeTempestEndTime(System.currentTimeMillis() + DURATION.toMillis());
        player.sendActionBar(join(separator(space()),
                          text(session.combat.getBladeTempestCharges(), talentType.skillType.textColor, BOLD),
                          text("Blade Tempest", talentType.skillType.textColor)));
        if (session.combat.getBladeTempestCooldown() > System.currentTimeMillis()) return; // only the first sweep triggers AoE
        session.combat.setBladeTempestCooldown(System.currentTimeMillis() + 50); // ignore other sweeps for the next tick
        double itemDamage = getBaseAttackDamage(item); //for debug purposes
        // total damage from player; includes base attack damage and str(+3/lvl)/weakness(-4/lvl) (does not incl. ench)
        double attributeDamage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        int sweepLevel = item.getEnchantmentLevel(Enchantment.SWEEPING_EDGE);
        double sweepMultiplier = sweepBaseDamage + sweepDamagePerLevel * sweepLevel;
        double damage = attributeDamage * sweepMultiplier;
        // Find targets
        List<Damageable> bladeTempestTargets = new ArrayList<>();
        for (Damageable target : player.getWorld().getNearbyLivingEntities(player.getLocation(), talentRadius)) {
            if (target instanceof Enemy && target.isValid() && player.hasLineOfSight(target)) {
                bladeTempestTargets.add(target);
            }
        }
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " attrDmg: " + attributeDamage + " itemDmg: " + itemDamage
                + "\nDmg w/o charges: " + damage + " Targets: " + bladeTempestTargets.size()
                + "\nFinal dmg: " + damage * (1 + session.combat.getBladeTempestCharges() * chargeMultiplier) + " Charges: " + session.combat.getBladeTempestCharges());
        }
        // Lock sweeping
        session.combat.setBladeTempestHit(true);
        // Damage targets
        for (Damageable target : bladeTempestTargets) {
            ((LivingEntity)target).setNoDamageTicks(0);
            target.damage(damage, player);
            ((LivingEntity)target).setNoDamageTicks(0);
        }
        // Release sweeping
        session.combat.setBladeTempestHit(false);
    }
};
