package com.cavetale.skills;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Table(name = "players")
public final class SQLPlayer {
    @Id
    Integer id;
    @Column(nullable = false, unique = true)
    UUID uuid;
    @Column(nullable = false)
    int talentPoints = 0;
    @Column(nullable = true)
    String json;

    public SQLPlayer() { }

    SQLPlayer(@NonNull final UUID uuid) {
        this.uuid = uuid;
    }
}
