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
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class StormCollapseTalent extends Talent {
    protected static final double damageMultiplier = 0.5;
    protected static final double talentRadius = 18;

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
                       "Requires fully charged critical hit with a Sweeping Edge sword");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (!isSword(item)) return;
        if (item.getEnchantmentLevel(Enchantment.SWEEPING_EDGE) == 0) return;
        if (!event.isCritical() || player.getAttackCooldown() != 1.0) return;
        Session session = sessionOf(player);
        if (session.combat.getBladeTempestEndTime() > System.currentTimeMillis() && session.combat.getBladeTempestCharges() != 0) {
            int charges = session.combat.getBladeTempestCharges();
            session.combat.setBladeTempestCharges(0);
            session.combat.setBladeTempestEndTime(0);
            player.sendActionBar(text("Blade Tempest", DARK_GRAY, STRIKETHROUGH));
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
                target.damage(damage, player);
                ((LivingEntity)target).setNoDamageTicks(0);
            }
        }
    }
}