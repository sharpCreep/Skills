package com.cavetale.skills.util;

import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

public final class Mobs {
    private static Set<EntityType> fireImmune = Set.of(
        EntityType.BLAZE,
        EntityType.GHAST,
        EntityType.MAGMA_CUBE,
        EntityType.STRIDER,
        EntityType.WARDEN,
        EntityType.WITHER,
        EntityType.WITHER_SKELETON,
        EntityType.ZOMBIFIED_PIGLIN,
        EntityType.ZOGLIN);

    private Mobs() { }

    public static boolean isFireImmune(Mob mob) {
        return (fireImmune.contains(mob.getType()) ? true : false);
        // FIRE_RESISTANCE potion not checked as mobs can still be set on fire
    }
};
