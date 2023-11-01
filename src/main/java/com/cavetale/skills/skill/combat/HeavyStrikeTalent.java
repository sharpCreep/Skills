package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class HeavyStrikeTalent extends Talent {
    protected static final double chancePerLevel = 0.05;
    protected final Random random = new Random(); //SkillsPlugin.java already has a threadlocal random, probably should use that one

    protected HeavyStrikeTalent() {
        super(TalentType.HEAVY_STRIKE);
    }

    @Override
    public String getDisplayName() {
        return "Heavy Strike";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Axes enchanted with Smite have a chance to deal double damage",
                       "Every level of Smite gives +" + chancePerLevel * 100 + "% chance"
                       + " to double your damage from :axe:axe attacks.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (item == null || !MaterialTags.AXES.isTagged(item.getType())
            || item.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD) <= 0
            || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        Session session = sessionOf(player);
        if (random.nextDouble() < item.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD) * chancePerLevel) {
            if (sessionOf(player).isDebugMode()) {
                player.sendMessage(talentType + " doubles: " + event.getDamage() + " damage");
            }
            event.setDamage(event.getDamage() * 2);
        }
    }
};
