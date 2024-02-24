package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.ItemCheck;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.lang.Math;
import org.bukkit.attribute.Attribute;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class WeaponArtistryTalent extends Talent {
    protected static final double damageScaler = 0.5;

    protected WeaponArtistryTalent() {
        super(TalentType.WEAPON_ARTISTRY);
    }

    @Override
    public String getDisplayName() {
        return "Weapon Artistry";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Having different enchantments on your weapons will increase your damage",
                       "Every level of an enchantment not present on the other weapon will add"
                       + " damage. Requires melee weapons in both hands.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        Session session = sessionOf(player);
        // Main Hand
        if (!ItemCheck.isCombatTool(item)) return;
        // Off Hand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (!ItemCheck.isCombatTool(offhand)) return;
        // Process enchantments
        Map<Enchantment, Integer> mainHandEnchanments = item.getEnchantments();
        Map<Enchantment, Integer> offHandEnchanments = offhand.getEnchantments();
        Set<Enchantment> enchSet = new HashSet<Enchantment>();
        int levelDifference = 0;
        for (Map.Entry<Enchantment, Integer> entry : mainHandEnchanments.entrySet()) {
            enchSet.add(entry.getKey());
        }
        for (Map.Entry<Enchantment, Integer> entry : offHandEnchanments.entrySet()) {
            if (enchSet.remove(entry.getKey())) {
                levelDifference += Math.abs(entry.getValue() - mainHandEnchanments.get(entry.getKey()));
            } else {
                levelDifference += entry.getValue();
            }
        }
        for (Enchantment ench : enchSet) {
            levelDifference += mainHandEnchanments.get(ench);
        }
        // Scale and add damage
        float attackCooldown = player.getAttackCooldown();
        double bonusDamage = levelDifference * damageScaler;
        event.setDamage(event.getDamage() + bonusDamage);
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " Ench diff: " + levelDifference + " +dmg: " + bonusDamage);
        }
    }
};
