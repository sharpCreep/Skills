package com.cavetale.skills.session;

import com.cavetale.skills.skill.SkillType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

@Getter
public final class CombatSession extends SkillSession {
    /** Remember for DenialTalent. */
    @Setter protected boolean poisonFreebie = false;
    /** Cooldown in Epoch Millis. */
    @Setter protected long godModeDuration = 0;
    /** ImpalerTalent target and current impale stacks. */
    @Setter protected int impalerTargetId = 0;
    @Setter protected int impalerStack = 0;
    /** OverkillTalent stored damage and talent phase (Add/Store) */
    @Setter protected double overkillDamage = 0;
    @Setter protected boolean overkillStore = false;
    /** LeapTalent cooldown */
    @Setter protected long leapCooldown = 0;
    /** BladeTempestTalent charges, times, trigger gating, scheduled task */
    @Setter protected int bladeTempestCharges = 0;
    @Setter protected long bladeTempestEndTime = 0;
    @Setter protected long bladeTempestLastTime = 0;
    @Setter protected long bladeTempestCooldown = 0;
    @Setter protected boolean bladeTempestHitInProgress = false;
    @Setter protected boolean bladeTempestExpiring = false;
    @Setter protected BukkitTask bladeTempestNextTask = null;

    protected CombatSession(final Session session) {
        super(session, SkillType.COMBAT);
    }
}
