package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.ItemCheck;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
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

public final class RelentlessAssaultTalent extends Talent {
    protected RelentlessAssaultTalent() {
        super(TalentType.RELENTLESS_ASSAULT);
    }

    @Override
    public String getDisplayName() {
        return "Relentless Assault";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Uncharged melee attacks deal more damage while dual wielding",
                       "Uncharged melee attacks deal more damage while dual wielding, up to"
                       + " your charged attack damage. Your melee hits ignore damage immunity.");
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
        ItemStack startingOffhand = player.getInventory().getItemInOffHand();
        if (!ItemCheck.isCombatTool(startingOffhand)) return;
        // Recalculate base damage (attribute, weapon+str)
        // newBase = min((attkCooldown^(1/2)+0.2), 1) * attribDmg
        float attackCooldown = player.getAttackCooldown();
        double attribDamage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        double newDamage = attribDamage * Math.min(0.2 + Math.pow((double)attackCooldown, 1/2d), 1d);
        if (event.isCritical()) newDamage *= 1.5;
        // Recalculate enchantment damage
        // newEnch = attkCd * enchDmg
        double enchantmentDamagePool = 0;
        EntityCategory mobCategory = mob.getCategory();
        int sharpLvl = item.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
        int smiteLvl = item.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD);
        int baneLvl = item.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS);
        int impaleLvl = item.getEnchantmentLevel(Enchantment.IMPALING);
        if (sharpLvl > 0) enchantmentDamagePool += 0.5 + 0.5 * sharpLvl;
        if (mobCategory == EntityCategory.UNDEAD && smiteLvl > 0) enchantmentDamagePool += 2.5 * smiteLvl;
        if (mobCategory == EntityCategory.ARTHROPOD && baneLvl > 0) enchantmentDamagePool += 2.5 * baneLvl;
        if (mobCategory == EntityCategory.WATER && impaleLvl > 0) enchantmentDamagePool += 2.5 * impaleLvl;
        if (mob.getType() == EntityType.DROWNED && impaleLvl > 0) enchantmentDamagePool += 2.5 * impaleLvl;
        // Set dmg
        newDamage += attackCooldown * enchantmentDamagePool;
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " Weapon: " + item.getType()
                + "\nOld: " + event.getDamage() + " New: " + newDamage + "\nCharge: " + attackCooldown);
        }
        // Reset immunity
        mob.setNoDamageTicks(0);
        event.setDamage(newDamage);
    }
};
