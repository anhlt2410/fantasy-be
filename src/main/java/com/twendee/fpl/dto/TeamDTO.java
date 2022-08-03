package com.twendee.fpl.dto;

import com.twendee.fpl.model.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeamDTO {
    private long id;
    private String name;
    private String fplName;
    private Long fplId;

    public TeamDTO(Team team) {
        this.id = team.getId();
        this.name = team.getName();
        this.fplName = team.getFplName();
        this.fplId = team.getFplId();
    }
}
