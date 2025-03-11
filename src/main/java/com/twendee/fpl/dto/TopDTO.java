package com.twendee.fpl.dto;

import com.twendee.fpl.model.GameWeekResult;
import com.twendee.fpl.model.Team;
import com.twendee.fpl.model.enumeration.TopType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopDTO {
    private Integer rank;
    private String topUserName;
    private String topUserFplName;
    private Integer data;

    public TopDTO(GameWeekResult gameWeekResult) {
        this.topUserName = gameWeekResult.getTeam().getName();
        this.topUserFplName = gameWeekResult.getTeam().getFplName();
        this.data = gameWeekResult.getLocalPoint();
    }

    public TopDTO(Team team) {
        this.rank = team.getPosition();
        this.topUserName = team.getName();
        this.topUserFplName = team.getFplName();
        this.data = team.getPoint();
    }

    public TopDTO(Team team, Integer data) {
        this.rank = team.getPosition();
        this.topUserName = team.getName();
        this.topUserFplName = team.getFplName();
        this.data = data;
    }
}
