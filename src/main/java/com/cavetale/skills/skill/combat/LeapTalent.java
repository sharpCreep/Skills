package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.time.Duration;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static org.bukkit.attribute.AttributeModifier.Operation.*;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class LeapTalent extends Talent implements Listener {
    protected static final int cooldown = 8;
    protected static final Duration DURATION = Duration.ofSeconds(cooldown);
    
    protected LeapTalent() {
        super(TalentType.LEAP);
    }

    @Override
    public String getDisplayName() {
        return "Leap";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Right click to perform a leap forward",
                       "Requires an axe or a sword in hand to use."
                       + " You need to be on solid ground to leap.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }

    //consider moving the listeners to CombatListener
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        if (event.hasBlock()) {
            Block block = event.getClickedBlock();
            if (block.getType().isInteractable()) return;
        }
        if (!player.isOnGround()) return;
        switch (event.getAction()) {
        case RIGHT_CLICK_BLOCK:
        case RIGHT_CLICK_AIR: {
            if (item.getType() == Material.WRITTEN_BOOK) {
                // Close inventories before opening any books because
                // InventoryCloseEvent is never called!
                player.closeInventory();
            }
            if (MaterialTags.AXES.isTagged(item.getType()) || MaterialTags.SWORDS.isTagged(item.getType())) {
                 //sprint+leap is a larger boost, should it be normalized?
                 //eating, blocking, aiming etc. from offhand happens in parallel to leaping
                 Session session = sessionOf(player);
                 long leapCooldownDuration = session.combat.getLeapCooldown();
                 long remainingCooldown = (leapCooldownDuration - System.currentTimeMillis())/1000;
                 if (remainingCooldown <= 0) {
                     event.setCancelled(true);
                     session.combat.setLeapCooldown(System.currentTimeMillis() + DURATION.toMillis());
                     performLeap(player);
                 } else {
                     player.sendActionBar(text("Cooldown " + (int) remainingCooldown + "s", DARK_RED));
                     player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 2.0f);
                     return;
                 }
            }
            break;
        }
        default: break;
        }
    }

    private void performLeap(Player player) {
        //Location leapFrom = player.getLocation();
        Vector facing = player.getEyeLocation().getDirection();
        Vector heading = new Vector(facing.getX(), 0 ,facing.getZ());
        heading.normalize();
        heading.setY(0.5); //30° upward, might need overall a larger force, scalar multiply then
        player.setVelocity(heading);
        return;
    }
};