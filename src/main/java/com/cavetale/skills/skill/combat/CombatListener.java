package com.cavetale.skills.skill.combat;

import com.cavetale.skills.util.Players;
import com.cavetale.skills.util.UUIDDataType;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;//
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;//

@RequiredArgsConstructor
final class CombatListener implements Listener {
    protected final CombatSkill combatSkill;

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onEntityDeath(EntityDeathEvent event) {
        // mob validity
        if (!(event.getEntity() instanceof Mob)) return;
        Mob mob = (Mob) event.getEntity();
        if (!mob.isDead()) return;
        // is player kill?
        PersistentDataContainer pdc = mob.getPersistentDataContainer();
        if (!pdc.has(combatSkill.COMBAT_RECENT_HIT_BY)
            || !pdc.has(combatSkill.ARCHERY_RECENT_HIT_BY)) return; // no player contribution
        boolean combatKill = combatSkill.wasLastHitCombat(pdc);
        // get player
        Player killer = null;
        if (combatKill) {
            UUID uuid = pdc.get(combatSkill.COMBAT_RECENT_HIT_BY, new UUIDDataType());
            killer = Bukkit.getPlayer(uuid);
        }
        combatSkill.onMeleeKill(killer, mob, combatKill, event);
    }

/*    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onEntityDamage(EntityDamageEvent event) {
        //skillsPlugin().getServer().broadcastMessage("<EDE> dmg: " + event.getDamage() + " cause: " + event.getCause());
        switch (event.getCause()) {
        case CUSTOM:
            if (event.getEntity() instanceof Mob mob)
                combatSkill.onCustomDamage(mob, event);
            break;
        default: return;
        }
    }*/

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            // Mob attacks player
            final Player player = (Player) event.getEntity();
            if (!Players.playMode(player)) return;
            switch (event.getCause()) {
            case ENTITY_ATTACK:
                if (event.getDamager() instanceof Mob mob) {
                    combatSkill.onMobDamagePlayer(player, mob, event);
                }
                break;
            default: return;
            }
        } else if (event.getEntity() instanceof Mob) {
            // Player attacks mob
            final Mob mob = (Mob) event.getEntity();
            switch (event.getCause()) {
            case ENTITY_ATTACK:
                if (event.getDamager() instanceof Player player) {
                    combatSkill.onPlayerDamageMob(player, mob, event);
                }
                break;
            case ENTITY_SWEEP_ATTACK:
                if (event.getDamager() instanceof Player player) {
                    combatSkill.onPlayerSweepMob(player, mob, event);
                }
                break;
            default: return;
            }
        }
    }
}
