package com.cavetale.skills.skill.archery;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class CrossbowHailTalent extends Talent implements Listener {
    public CrossbowHailTalent() {
        super(TalentType.XBOW_HAIL);
    }

    @Override
    public String getDisplayName() {
        return "Hailstorm";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Crossbow bullets fired way up high will rain down.",
                       "You can shoot :arrow:arrows with your"
                       + " :crossbow:crossbow way up in the air"
                       + " and watch them rain down on your opponents."
                       + "\n\nThese arrows will not hurt yourself.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.ANVIL);
    }

    protected void onShootCrossbow(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> {
                if (!arrow.isValid() || arrow.isDead()) return;
                if (arrow.getLocation().getY() < player.getLocation().getY() + 10) return;
                arrow.setVelocity(new Vector(0.0, -3.2, 0.0));
                ArrowType.HAIL.set(arrow);
            }, 20L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onProjectileCollide(ProjectileCollideEvent event) {
        if (event.getEntity() instanceof AbstractArrow arrow
            && Objects.equals(arrow.getShooter(), event.getCollidedWith())
            && ArrowType.HAIL.is(arrow)) {
            event.setCancelled(true);
        }
    }
}
