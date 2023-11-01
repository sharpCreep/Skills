package com.cavetale.skills.skill;

import com.cavetale.skills.SkillsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public abstract class TalentTask extends BukkitRunnable {
    protected SkillsPlugin plugin;
    protected Player player;

/*    protected TalentTask(SkillsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }*/

//    public abstract void run(); // BukkitRunnable should already have this though
}