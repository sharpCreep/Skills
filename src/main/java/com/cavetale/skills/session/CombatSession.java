package com.cavetale.skills.session;

import com.cavetale.skills.skill.SkillType;
import lombok.Getter;
import lombok.Setter;

@Getter
public final class CombatSession extends SkillSession {
    /** Remember for DenialTalent. */
    @Setter protected boolean poisonFreebie = false;
    /** Cooldown in Epoch Millis. */
    @Setter protected long godModeDuration = 0;

    protected CombatSession(final Session session, final SkillType skillType) {
        super(session, skillType);
    }
}
