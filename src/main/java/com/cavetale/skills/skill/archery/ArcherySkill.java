package com.cavetale.skills.skill.archery;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.Skills;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.combat.CombatSkill;
import com.cavetale.skills.skill.combat.CombatReward;
import com.cavetale.skills.util.Players;
import com.cavetale.skills.util.UUIDDataType;
import java.util.UUID;
import java.util.Set;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import static com.cavetale.skills.SkillsPlugin.moneyBonusPercentage;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static com.cavetale.skills.skill.combat.CombatReward.combatReward;
import static com.cavetale.skills.skill.combat.CombatSkill.addKillAndCheckCooldown;

/**
 * The Archery Skill.
 * Arrow#isCritical <=> EntityShootBowEvent.getForce() == 1.0
 * Arrow#getDamage() == 2.0 (always), Power: +1, +1.5, +2, +2.5, +3
 * Power: (lvl + 1) / 2 (wiki says /4, meaning hearts!)
 * Power Crit: 6, 9, 11, 12, 14, 15
 * AbstractArrow <- Arrow/SpectralArrow
 * TippedArrow is deprecated and now in Arrow!
 */
public final class ArcherySkill extends Skill implements Listener {
    // Right
    public final ArcherZoneTalent archerZoneTalent = new ArcherZoneTalent();
    public final ArcherZoneDeathTalent archerZoneDeathTalent = new ArcherZoneDeathTalent();
    public final ArrowSwiftnessTalent arrowSwiftnessTalent = new ArrowSwiftnessTalent();
    public final BonusArrowTalent bonusArrowTalent = new BonusArrowTalent();
    public final ArrowDamageTalent arrowDamageTalent = new ArrowDamageTalent();
    public final ArrowVelocityTalent arrowVelocityTalent = new ArrowVelocityTalent();
    // Up
    public final ArrowMagnetTalent arrowMagnetTalent = new ArrowMagnetTalent();
    public final InfinityMendingTalent infinityMendingTalent = new InfinityMendingTalent();
    public final InstantHitTalent instantHitTalent = new InstantHitTalent();
    // Left
    public final CrossbowInfinityTalent crossbowInfinityTalent = new CrossbowInfinityTalent();
    public final CrossbowVolleyTalent crossbowVolleyTalent = new CrossbowVolleyTalent();
    public final CrossbowFlameTalent crossbowFlameTalent = new CrossbowFlameTalent();
    public final CrossbowHailTalent crossbowHailTalent = new CrossbowHailTalent();
    public final CrossbowLingerTalent crossbowLingerTalent = new CrossbowLingerTalent();
    public final CrossbowPierceTalent crossbowPierceTalent = new CrossbowPierceTalent();
    public final CrossbowDualTalent crossbowDualTalent = new CrossbowDualTalent();
    // Down
    public final TippedInfinityTalent tippedInfinityTalent = new TippedInfinityTalent();
    public final SpectralInfinityTalent spectralInfinityTalent = new SpectralInfinityTalent();
    public final GlowMarkTalent glowMarkTalent = new GlowMarkTalent();

    // TODO Duplicated from CombatSkill.java, needs to be moved to Skill.java from both
    // Combat PDC keys
    public static final NamespacedKey COMBAT_RECENT_HIT_BY = NamespacedKey.fromString("skills:combat_recent_hit_by"); // last damaged by player UUID
    public static final NamespacedKey COMBAT_TIME = NamespacedKey.fromString("skills:combat_time");
    public static final NamespacedKey COMBAT_TAGLIST = NamespacedKey.fromString("skills:combat_taglist"); // points to PDC with player UUIDs
    public static final String COMBAT_TAGGED_BY = "skills:combat_tagged_by_"; // prefix used for keys in taglist
    // Archery PDC keys
    public static final NamespacedKey ARCHERY_RECENT_HIT_BY = NamespacedKey.fromString("skills:archery_recent_hit_by");
    public static final NamespacedKey ARCHERY_TIME = NamespacedKey.fromString("skills:archery_time");
    public static final NamespacedKey ARCHERY_TAGLIST = NamespacedKey.fromString("skills:archery_taglist");
    public static final String ARCHERY_TAGGED_BY = "skills:archery_tagged_by_";


    public ArcherySkill() {
        super(SkillType.ARCHERY);
    }

    @Override
    public void enable() {
    }

    private void onArrowKill(Player player, AbstractArrow arrow, Mob mob, EntityDeathEvent event) {
        CombatReward reward = addKillAndCheckCooldown(mob.getLocation())
            ? null
            : combatReward(mob);
        if (arrow == null) {
            // not killed by an arrow, but most recent player combat interaction was archery
            // give SP
            PersistentDataContainer pdc = mob.getPersistentDataContainer();
            PersistentDataContainer pdc_taglist = pdc.get(CombatSkill.ARCHERY_TAGLIST, PersistentDataType.TAG_CONTAINER);
            Set<NamespacedKey> taglist = pdc_taglist.getKeys();
            int rewardShare = (reward.sp - 1) / taglist.size() + 1;
            for (NamespacedKey key : taglist) {
                Player tagger = Bukkit.getPlayer(pdc_taglist.get(key, new UUIDDataType()));
                if (/*TODO*//*CombatSkill.checkPlayer(tagger)*/tagger != null && tagger.isConnected() && Players.playMode(tagger) && sessionOf(tagger).isEnabled()) sessionOf(tagger).addSkillPoints(SkillType.ARCHERY, rewardShare);
            }
        } else {
            if (!arrow.isShotFromCrossbow()) {
                archerZoneDeathTalent.onBowKill(player, arrow, mob);
            }
            final boolean hasMagnet = arrowMagnetTalent.isPlayerEnabled(player);
            if (reward != null) {
                if (reward.money > 0) {
                    int bonus = sessionOf(player).getMoneyBonus(skillType);
                    double factor = 1.0 + 0.01 * moneyBonusPercentage(bonus);
                    Location location = hasMagnet ? player.getLocation() : mob.getLocation();
                    dropMoney(player, location, reward.money * factor);
                }
                event.setDroppedExp(3 * event.getDroppedExp() + sessionOf(player).getExpBonus(SkillType.ARCHERY));
            }
            if (hasMagnet) {
                int exp = event.getDroppedExp();
                event.setDroppedExp(0);
                player.giveExp(exp, true);
                List<ItemStack> drops = List.copyOf(event.getDrops());
                event.getDrops().clear();
                for (ItemStack drop : drops) {
                    Item item = player.getWorld().dropItem(player.getLocation(), drop);
                    item.setPickupDelay(0);
                    item.setOwner(player.getUniqueId());
                }
            }
        }
/*        if (arrow != null && !arrow.isShotFromCrossbow()) {
            archerZoneDeathTalent.onBowKill(player, arrow, mob);
        }*/
//        combat.checkPlayer(player)
//        Session session = sessionOf(player);
/*        CombatReward reward = addKillAndCheckCooldown(mob.getLocation())
            ? null
            : combatReward(mob);*/
/*        final boolean hasMagnet = arrowMagnetTalent.isPlayerEnabled(player);
        if (reward != null) {
            session.addSkillPoints(skillType, reward.sp);
            if (reward.money > 0) {
                int bonus = session.getMoneyBonus(skillType);
                double factor = 1.0 + 0.01 * moneyBonusPercentage(bonus);
                Location location = hasMagnet ? player.getLocation() : mob.getLocation();
                dropMoney(player, location, reward.money * factor);
            }
            event.setDroppedExp(3 * event.getDroppedExp() + session.getExpBonus(SkillType.COMBAT));
        }
        if (hasMagnet) {
            int exp = event.getDroppedExp();
            event.setDroppedExp(0);
            player.giveExp(exp, true);
            List<ItemStack> drops = List.copyOf(event.getDrops());
            event.getDrops().clear();
            for (ItemStack drop : drops) {
                Item item = player.getWorld().dropItem(player.getLocation(), drop);
                item.setPickupDelay(0);
                item.setOwner(player.getUniqueId());
            }
        }*/
    }

    private void onArrowDamage(Player player, AbstractArrow arrow, Mob mob, EntityDamageByEntityEvent event) {
        //TODO: tag hit mob
        archeryTagMob(mob, player);
        if (!arrow.isShotFromCrossbow()) {
            archerZoneTalent.onBowDamage(player, arrow, mob);
            bonusArrowTalent.onBowDamage(player, arrow, mob);
        }
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(skillType + " onArrowDamage "
                               + " arrowDmg=" + arrow.getDamage()
                               + " eventDmg=" + event.getDamage()
                               + " finalDmg=" + event.getFinalDamage()
                               + " crit=" + arrow.isCritical()
                               + " primary=" + ArrowType.PRIMARY.is(arrow)
                               + " bonus=" + ArrowType.BONUS.is(arrow));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (event.getDamager() instanceof AbstractArrow arrow
            && !(arrow instanceof Trident)
            && arrow.getShooter() instanceof Player player
            && isPlayerEnabled(player)) {
            onArrowDamage(player, arrow, mob, event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;
        Mob mob = (Mob) event.getEntity();
        if (!mob.isDead()) return;
        // move to getting killer from PDC, + if it is an archery kill
        PersistentDataContainer pdc = mob.getPersistentDataContainer();
        if (!pdc.has(CombatSkill.COMBAT_RECENT_HIT_BY)
            || !pdc.has(CombatSkill.ARCHERY_RECENT_HIT_BY)) return; // no player contribution
        boolean archeryKill = wasLastHitArchery(pdc);
        Player killer = null;
        if (archeryKill) {
            UUID uuid = pdc.get(CombatSkill.ARCHERY_RECENT_HIT_BY, new UUIDDataType());
            killer = Bukkit.getPlayer(uuid);
        }
// TODO: is an archeryKill flag needed to be passed on? right now arrow being null is used
        switch (mob.getLastDamageCause().getCause()) {
        case PROJECTILE:
            EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) mob.getLastDamageCause();
            if (edbee.getDamager() instanceof AbstractArrow arrow
                && !(arrow instanceof Trident)
                && arrow.getShooter() instanceof Player player
                && isPlayerEnabled(player)) {
                onArrowKill(player, arrow, mob, event);
            } else {
                onArrowKill(killer, null, mob, event);
            }
            return;
        default: break; // TODO: give SP in all other cases too - separate out SP rewards?
        }
        /*
        if (!(mob.getLastDamageCause() instanceof EntityDamageByEntityEvent edbee)) return;
        switch (edbee.getCause()) {
        case PROJECTILE:
            if (edbee.getDamager() instanceof AbstractArrow arrow
                && !(arrow instanceof Trident)
                && arrow.getShooter() instanceof Player player
                && isPlayerEnabled(player)) {
                onArrowKill(player, arrow, mob, event);
            }
            return;
        default: break;
        }*/
    }

    /**
     * Mark shot arrows as primary.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isPlayerEnabled(player)) return;
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        ItemStack bow = event.getBow();
        if (bow == null) return;
        ArrowType.PRIMARY.set(arrow);
        if (bow.getType() == Material.BOW) {
            ItemStack consumable = event.getConsumable();
            onShootBow(player, arrow);
            tippedInfinityTalent.onShootBow(player, bow, consumable, arrow, event);
            spectralInfinityTalent.onShootBow(player, bow, consumable, arrow, event);
        } else if (bow.getType() == Material.CROSSBOW) {
            // Called 3 times in case of Multishot
            onShootCrossbow(player, arrow);
            // The ones below we do not want to be called for custom
            // arrows.  Flame would be a consideration, but it will
            // simply adopt the flame status of the original arrow,
            // which is a simple solution.
            // The order matters:
            // - Apply flame first
            // - Volley will copy the flame state
            // - Infinity will DISALLOW the pickup status (volley)
            crossbowFlameTalent.onShootCrossbow(player, bow, arrow);
            crossbowVolleyTalent.onShootCrossbow(player, bow, arrow);
            crossbowInfinityTalent.onShootCrossbow(player, bow, arrow);
            crossbowDualTalent.onShootCrossbow(player);
        }
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(skillType + " " + event.getEventName()
                               + " " + bow.getType()
                               + " velo:" + arrow.getVelocity().length()
                               + " dmg:" + arrow.getDamage()
                               + " crit:" + arrow.isCritical()
                               + " force:" + event.getForce()
                               + " fire:" + arrow.getFireTicks()
                               + " consume:" + event.shouldConsumeItem());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow) || arrow instanceof Trident) return;
        if (!(arrow.getShooter() instanceof Player player)) return;
        if (event.getHitBlock() != null) {
            archerZoneTalent.onArrowHitBlock(player, arrow);
            crossbowLingerTalent.onArrowHitBlock(player, arrow, event);
            if (arrow.isDead()) return;
            if (ArrowType.BONUS.is(arrow) || ArrowType.SPAM.is(arrow)) {
                Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> arrow.remove(), 10L);
            } else if (arrow.getPickupStatus() != AbstractArrow.PickupStatus.ALLOWED) {
                Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> arrow.remove(), 10L);
            }
        } else if (event.getHitEntity() instanceof LivingEntity target) {
            arrowDamageTalent.onArrowCollide(player, arrow);
            glowMarkTalent.onArrowCollide(player, arrow, target);
            instantHitTalent.onArrowCollide(player, arrow, target);
        }
    }

    /**
     * Called by onEntityShootBow and BonusArrowTalent.
     */
    protected void onShootBow(Player player, AbstractArrow arrow) {
        archerZoneTalent.onShootBow(player, arrow);
        arrowSwiftnessTalent.onShootBow(player, arrow);
        arrowVelocityTalent.onShootBow(player, arrow);
    }

    /**
     * Contains functions to be called for every crossbow arrow.
     * Called by onEntityShootBow and CrossbowVolleyTalent.
     */
    protected void onShootCrossbow(Player player, AbstractArrow arrow) {
        crossbowHailTalent.onShootCrossbow(player, arrow);
    }

    protected static void archeryTagMob(Mob mob, Player player) {
        if (!mob.isValid()) return; // prevent tagging a dying mob; should never happen
        PersistentDataContainer pdc = mob.getPersistentDataContainer();
        pdc.set(CombatSkill.ARCHERY_RECENT_HIT_BY, new UUIDDataType(), player.getUniqueId());
        pdc.set(CombatSkill.ARCHERY_TIME, PersistentDataType.LONG, System.currentTimeMillis());
        PersistentDataContainer pdc_taglist = null;
        if (!pdc.has(CombatSkill.ARCHERY_TAGLIST)) {
            pdc_taglist = pdc.getAdapterContext().newPersistentDataContainer();
            pdc.set(CombatSkill.ARCHERY_TAGLIST, PersistentDataType.TAG_CONTAINER, pdc_taglist);
        } else {
            pdc_taglist = pdc.get(CombatSkill.ARCHERY_TAGLIST, PersistentDataType.TAG_CONTAINER);
        }
        pdc_taglist.set(NamespacedKey.fromString(CombatSkill.ARCHERY_TAGGED_BY + player.getName()), new UUIDDataType(), player.getUniqueId());
    }

    // nearly a duplicate of CombatSkill.wasLastHitCombat(pdc) TODO: move both to Skill
    public boolean wasLastHitArchery(PersistentDataContainer pdc) {
        if (!pdc.has(ARCHERY_TIME)) return false;
        if (pdc.has(COMBAT_TIME)) {
            long combatTimeStamp = pdc.get(COMBAT_TIME, PersistentDataType.LONG);
            long archeryTimeStamp = pdc.get(ARCHERY_TIME, PersistentDataType.LONG);
            return archeryTimeStamp > combatTimeStamp;
        }
        return true;
    }
}
