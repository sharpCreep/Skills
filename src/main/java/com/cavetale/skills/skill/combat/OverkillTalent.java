package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class OverkillTalent extends Talent {
    protected OverkillTalent() {
        super(TalentType.OVERKILL);
    }

    @Override
    public String getDisplayName() {
        return "Overkill";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Leftover damage from an axe kill is added to your next axe attack",
                       "Breaking your focus will reset the damage bonus."
                       + " Break focus by switching items or taking damage."); // missing an attack?
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isPlayerEnabled(player)) return;
        resetOverkill(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        resetOverkill(player);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (item == null || !MaterialTags.AXES.isTagged(item.getType())
        || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        Session session = sessionOf(player);
        double bonusDamage = session.combat.getOverkillDamage();
        // Use up stored damage on the attack
        if (!session.combat.isOverkillStore()) {
		    if (bonusDamage > 0) {
		        if (sessionOf(player).isDebugMode()) {
		            player.sendMessage(talentType + " base: " + event.getFinalDamage() + " +dmg: " + bonusDamage);
		        }
		        event.setDamage(event.getDamage() + bonusDamage);
		        session.combat.setOverkillDamage(0);
		    }
            session.combat.setOverkillStore(true);
            return;
        }
        // Check if it is a killing blow and store damage if so
        if (session.combat.isOverkillStore()) {
            if (mob.getHealth() <= event.getFinalDamage()) {
                session.combat.setOverkillDamage(event.getFinalDamage() - mob.getHealth());
                if (sessionOf(player).isDebugMode()) {
                    player.sendMessage(talentType + " stored: " + bonusDamage + " -> " + session.combat.getOverkillDamage());
                }
            }
        	session.combat.setOverkillStore(false);
        }
        return;
    }

    protected void resetOverkill(Player player) {
        Session session = sessionOf(player);
        if (session.combat.getOverkillDamage() == 0) return;
        session.combat.setOverkillDamage(0);
        session.combat.setOverkillStore(false);
    }
};
