package com.cavetale.skills.skill.combat;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.util.Players;
import com.cavetale.skills.util.UUIDDataType;
import com.cavetale.worldmarker.util.Tags;
import java.util.UUID;
import java.util.Set;
import java.time.Duration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import static com.cavetale.skills.SkillsPlugin.moneyBonusPercentage;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static com.cavetale.skills.skill.combat.CombatReward.combatReward;

public final class CombatSkill extends Skill {
    protected final CombatListener combatListener = new CombatListener(this);;
    public final SearingTalent searingTalent = new SearingTalent();
    public final PyromaniacTalent pyromaniacTalent = new PyromaniacTalent();
    public final DenialTalent denialTalent = new DenialTalent();
    public final GodModeTalent godModeTalent = new GodModeTalent();
    public final IronAgeTalent ironAgeTalent = new IronAgeTalent();
    public final ExecutionerTalent executionerTalent = new ExecutionerTalent();
    public final ImpalerTalent impalerTalent = new ImpalerTalent();
    public final ToxicistTalent toxicistTalent = new ToxicistTalent();
    public final ToxicFurorTalent toxicFurorTalent = new ToxicFurorTalent();
    // Combat update
    public final HeadtakerTalent headtakerTalent = new HeadtakerTalent();
    public final HeavyStrikeTalent heavyStrikeTalent = new HeavyStrikeTalent();
    public final OverkillTalent overkillTalent = new OverkillTalent();
    public final LeapTalent leapTalent = new LeapTalent();
    public final FirestarterTalent firestarterTalent = new FirestarterTalent();
    public final ViciousMomentumTalent viciousMomentumTalent = new ViciousMomentumTalent();
    public final BladeTempestTalent bladeTempestTalent = new BladeTempestTalent();
    public final StormCollapseTalent stormCollapseTalent = new StormCollapseTalent();

    // TODO Duplicated in ArcherySkill.java, needs to be moved to Skill.java from both
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

    protected static final long CHUNK_KILL_DECAY_TIME = Duration.ofMinutes(5).toMillis();

    public CombatSkill() {
        super(SkillType.COMBAT);
    }

    @Override
    protected void enable() {
        MobStatusEffect.enable();
        Bukkit.getPluginManager().registerEvents(combatListener, skillsPlugin());
    }

    /**
     * Mob damages a player, talent precedence.
     */
    protected void onMobDamagePlayer(Player player, Mob mob, EntityDamageByEntityEvent event) {
        searingTalent.onMobDamagePlayer(player, mob, event);
        denialTalent.onMobDamagePlayer(player, mob, event);
    }

    /**
     * Player damages a mob, talent precedence.
     */
    protected void onPlayerDamageMob(Player player, Mob mob, EntityDamageByEntityEvent event) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        combatTagMob(mob, player);
        // utility before damage calculation
        denialTalent.onPlayerDamageMob(player, mob, item, event);
        denialTalent.onPlayerDamageMob(player, mob, item, event); //is it needed twice?
        firestarterTalent.onPlayerDamageMob(player, mob, item, event);
        viciousMomentumTalent.onPlayerDamageMob(player, mob, item, event);
        stormCollapseTalent.onPlayerDamageMob(player, mob, item, event);
        // additive damage
        ironAgeTalent.onPlayerDamageMob(player, mob, item, event);
        executionerTalent.onPlayerDamageMob(player, mob, item, event);
        impalerTalent.onPlayerDamageMob(player, mob, item, event);
        toxicistTalent.onPlayerDamageMob(player, mob, item, event);
        toxicFurorTalent.onPlayerDamageMob(player, mob, item, event);
        overkillTalent.onPlayerDamageMob(player, mob, item, event); // add damage
        // multiplicative damage
        heavyStrikeTalent.onPlayerDamageMob(player, mob, item, event);
        // multiplicative after damage reduction effects
        pyromaniacTalent.onPlayerDamageMob(player, mob, item, event);
        // utility after damage calculation
        overkillTalent.onPlayerDamageMob(player, mob, item, event); // store damage
        headtakerTalent.onPlayerDamageMob(player, mob, item, event);
    }

    /**
     * Player damages a mob with a sweep attack, talent precedence.
     */
    protected void onPlayerSweepMob(Player player, Mob mob, EntityDamageByEntityEvent event) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        combatTagMob(mob, player);
        bladeTempestTalent.onPlayerDamageMob(player, mob, item, event);
    }

    /**
     * Mob is damaged with CUSTOM damage, talent precedence.
     */
/*    protected void onCustomDamage(Mob mob, EntityDamageEvent event) {
        // tagging needs to be handled by the talent when dealing damage()
        PersistentDataContainer pdc = mob.getPersistentDataContainer();
        if (!wasLastHitCombat(pdc)) return;
        Player player = Bukkit.getPlayer(pdc.get(COMBAT_RECENT_HIT_BY, new UUIDDataType()));
        if (!checkPlayer(player)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        // multiplicative damage
        bladeTempestTalent.onPlayerDamageMob(player, mob, item, event); // +1% per charge
    }
*/

    /**
     * Player kills a mob, talent precedence. Give skill points and money.
     */
    protected void onMeleeKill(Player player, Mob mob, boolean meleeKill, EntityDeathEvent event) {
        PersistentDataContainer pdc = mob.getPersistentDataContainer();
        //boolean combatKill = wasLastHitCombat(pdc); // moved to listener
        boolean killerValid = meleeKill && checkPlayer(player);
        boolean landedKillingBlow = player == mob.getKiller();
        // talent activation
        if (killerValid && landedKillingBlow) {
            switch (mob.getLastDamageCause().getCause()) {
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                godModeTalent.onMeleeKill(player, mob);
                return;
            //case CUSTOM: // currently unused, only dealt by Blade Tempest
            case PROJECTILE: // currently unused, thrown tridents
                if (mob.getLastDamageCause() instanceof EntityDamageByEntityEvent edbee
                    && edbee.getDamager() instanceof Trident) {
                }
            default: break;
            }
        }
        CombatReward reward = combatReward(mob);
        if (reward == null) return;
        if (mob instanceof Ageable && !((Ageable) mob).isAdult()) return; //baby zombies?
        if (addKillAndCheckCooldown(mob.getLocation())) return;
        // give SP
        PersistentDataContainer pdc_taglist = pdc.get(COMBAT_TAGLIST, PersistentDataType.TAG_CONTAINER);
        Set<NamespacedKey> taglist = pdc_taglist.getKeys();
        int rewardShare = (2 * reward.sp - 1) / taglist.size() + 1;
        for (NamespacedKey key : taglist) {
            Player tagger = Bukkit.getPlayer(pdc_taglist.get(key, new UUIDDataType()));
            if (checkPlayer(tagger)) sessionOf(tagger).addSkillPoints(SkillType.COMBAT, rewardShare);
        }
        if (killerValid) {
            Session session = sessionOf(player);
            // Exp bonus
            event.setDroppedExp(3 * event.getDroppedExp() + session.getExpBonus(SkillType.COMBAT));
            // Money bonus
            if (reward.money > 0) {
                int bonus = session.getMoneyBonus(SkillType.COMBAT);
                double factor = 1.0 + 0.01 * moneyBonusPercentage(bonus);
                dropMoney(player, mob.getLocation(), reward.money * factor);
            }
            // if landed killing blow, apply fortune to deaths with CUSTOM dmg cause (talent dmg)
            // combat magnet
        }
    }

    private static final NamespacedKey KEY_KILLS = NamespacedKey.fromString("skills:kills");
    private static final NamespacedKey KEY_LAST_KILL = NamespacedKey.fromString("skills:last_kill");

    /**
     * Add kill and check cooldown.
     * @return true if location (chunk) is on cooldown
     */
    public static boolean addKillAndCheckCooldown(Location location) {
        if (NetworkServer.current() == NetworkServer.MOB_ARENA) return false;
        final PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();
        final long now = System.currentTimeMillis();
        Integer oldKills = Tags.getInt(pdc, KEY_KILLS);
        if (oldKills == null) oldKills = 0;
        Long lastKill = Tags.getLong(pdc, KEY_LAST_KILL);
        if (lastKill == null) lastKill = 0L;
        long subtraction = (now - lastKill) / CHUNK_KILL_DECAY_TIME;
        final int kills = Math.max(0, oldKills - (int) subtraction) + 1;
        Tags.set(pdc, KEY_KILLS, kills);
        Tags.set(pdc, KEY_LAST_KILL, now);
        return kills > 5;
    }

    protected static void combatTagMob(Mob mob, Player player) {
        if (!mob.isValid()) return; // prevent tagging a dying mob; should never happen
        PersistentDataContainer pdc = mob.getPersistentDataContainer();
        pdc.set(COMBAT_RECENT_HIT_BY, new UUIDDataType(), player.getUniqueId());
        pdc.set(COMBAT_TIME, PersistentDataType.LONG, System.currentTimeMillis());
        PersistentDataContainer pdc_taglist = null;
        if (!pdc.has(COMBAT_TAGLIST)) {
            pdc_taglist = pdc.getAdapterContext().newPersistentDataContainer();
            pdc.set(COMBAT_TAGLIST, PersistentDataType.TAG_CONTAINER, pdc_taglist);
        } else {
            pdc_taglist = pdc.get(COMBAT_TAGLIST, PersistentDataType.TAG_CONTAINER);
        }
        pdc_taglist.set(NamespacedKey.fromString(COMBAT_TAGGED_BY + player.getName()), new UUIDDataType(), player.getUniqueId());
    }

    public boolean checkPlayer(Player player) {
        return player != null && player.isConnected() && Players.playMode(player) && sessionOf(player).isEnabled();
    }

    // TODO near duplicate in ArcherySkill, move both to Skill
    public boolean wasLastHitCombat(PersistentDataContainer pdc) {
        if (!pdc.has(COMBAT_TIME)) return false;
        if (pdc.has(ARCHERY_TIME)) {
            long combatTimeStamp = pdc.get(COMBAT_TIME, PersistentDataType.LONG);
            long archeryTimeStamp = pdc.get(ARCHERY_TIME, PersistentDataType.LONG);
            return combatTimeStamp > archeryTimeStamp;
        }
        return true;
    }
}
