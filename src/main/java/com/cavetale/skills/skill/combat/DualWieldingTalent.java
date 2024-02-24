package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.ItemCheck;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class DualWieldingTalent extends Talent {
    protected DualWieldingTalent() {
        super(TalentType.DUAL_WIELDING);
    }

    @Override
    public String getDisplayName() {
        return "Dual Wielding";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Automatically swap weapons after attacks",
                       "Swaps your mainhand and offhand melee weapons after"
                       + " a successful melee attack.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        Session session = sessionOf(player);
        if (session.combat.getDualWieldingLock() > System.currentTimeMillis()) return;
        // Main Hand
        if (!ItemCheck.isCombatTool(item)) return;
        // Off Hand
        ItemStack startingOffhand = player.getInventory().getItemInOffHand();
        if (!ItemCheck.isCombatTool(startingOffhand)) return;
        // Lock and schedule swap
        session.combat.setDualWieldingLock(System.currentTimeMillis() + 20 * (session.combat.getDualWieldingDelay() + 1));
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " Delay in ticks: " + (session.combat.getDualWieldingDelay() + 1));
        }
        Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> {
                if (!player.isOnline()) return;
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (!ItemCheck.isCombatTool(hand)) return;
                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (!ItemCheck.isCombatTool(offhand)) return;
                player.getInventory().setItemInOffHand(hand);
                player.getInventory().setItemInMainHand(offhand);
            }, session.combat.getDualWieldingDelay() + 1);
        session.combat.setDualWieldingDelay(0);
    }
};
