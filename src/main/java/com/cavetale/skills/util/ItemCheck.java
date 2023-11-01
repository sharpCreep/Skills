package com.cavetale.skills.util;

import com.destroystokyo.paper.MaterialTags;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
/*import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;*/

public final class ItemCheck {
    /*private static Set<Material> itemClass = Set.of(
        Material._,
        Material._);*/
    private static final Map<Material, Double> weaponDamageMap = Map.ofEntries(
        //SWORD
        Map.entry(Material.WOODEN_SWORD, 4d),
        Map.entry(Material.STONE_SWORD, 5d),
        Map.entry(Material.IRON_SWORD, 6d),
        Map.entry(Material.GOLDEN_SWORD, 4d),
        Map.entry(Material.DIAMOND_SWORD, 7d),
        Map.entry(Material.NETHERITE_SWORD, 8d),
        //AXE
        Map.entry(Material.WOODEN_AXE, 7d),
        Map.entry(Material.STONE_AXE, 9d),
        Map.entry(Material.IRON_AXE, 9d),
        Map.entry(Material.GOLDEN_AXE, 7d),
        Map.entry(Material.DIAMOND_AXE, 9d),
        Map.entry(Material.NETHERITE_AXE, 10d),
        //HOE
        Map.entry(Material.WOODEN_HOE, 1d),
        Map.entry(Material.STONE_HOE, 1d),
        Map.entry(Material.IRON_HOE, 1d),
        Map.entry(Material.GOLDEN_HOE, 1d),
        Map.entry(Material.DIAMOND_HOE, 1d),
        Map.entry(Material.NETHERITE_HOE, 1d),
        //PICKAXE
        Map.entry(Material.WOODEN_PICKAXE, 2d),
        Map.entry(Material.STONE_PICKAXE, 3d),
        Map.entry(Material.IRON_PICKAXE, 4d),
        Map.entry(Material.GOLDEN_PICKAXE, 2d),
        Map.entry(Material.DIAMOND_PICKAXE, 5d),
        Map.entry(Material.NETHERITE_PICKAXE, 6d),
        //SHOVEL
        Map.entry(Material.WOODEN_SHOVEL, 2.5d),
        Map.entry(Material.STONE_SHOVEL, 3.5d),
        Map.entry(Material.IRON_SHOVEL, 4.5d),
        Map.entry(Material.GOLDEN_SHOVEL, 2.5d),
        Map.entry(Material.DIAMOND_SHOVEL, 5.5d),
        Map.entry(Material.NETHERITE_SHOVEL, 6.5d),
        //OTHER
        Map.entry(Material.TRIDENT, 9d));

    private ItemCheck() { }

/*    public static boolean is_(ItemStack item) {
        return ((MaterialTags._.isTagged(item.getType())
            || (item.getType() == Material._)
            || itemClass.contains(item)) ? true : false);
    }*/

    public static boolean isSword(ItemStack item) {
        if (item == null) return false;
        return (MaterialTags.SWORDS.isTagged(item.getType()) ? true : false);
    }

    public static boolean isAxe(ItemStack item) {
        if (item == null) return false;
        return (MaterialTags.AXES.isTagged(item.getType()) ? true : false);
    }

    public static boolean isMeleeWeapon(ItemStack item) {
        return ((isAxe(item) || isSword(item)) ? true : false);
    }

    public static boolean isCombatWeapon(ItemStack item) {
        if (item == null) return false;
        return ((isMeleeWeapon(item) || (item.getType() == Material.TRIDENT)) ? true : false);
    }

    public static boolean isCombatTool(ItemStack item) {
        if (item == null) return false;
        return ((isCombatWeapon(item)
            || MaterialTags.HOES.isTagged(item.getType())
            || MaterialTags.PICKAXES.isTagged(item.getType())
            || MaterialTags.SHOVELS.isTagged(item.getType())) ? true : false);
    }

//might not be worth it, this is usually followed up with getting the level
//+ to use this Enchantment is already imported
    public static boolean hasEnchantment(ItemStack item, Enchantment enchantment) {
        if (item == null) return false;
        return (item.getEnchantmentLevel(enchantment) > 0 ? true : false);
    }

    public static double getBaseAttackDamage(ItemStack item) {
        return (isCombatTool(item)) ? weaponDamageMap.get(item.getType()) : 1;
    }



































};
