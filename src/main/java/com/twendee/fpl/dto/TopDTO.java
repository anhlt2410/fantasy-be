package com.twendee.fpl.dto;

import com.twendee.fpl.model.GameWeekResult;
import com.twendee.fpl.model.enumeration.TopType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopDTO {
    private TopType type;
    private String topUserName;
    private String topUserFplName;
    private Integer data;

    public TopDTO(GameWeekResult gameWeekResult) {
        this.type = TopType.GAME_WEEK_POINT;
        this.topUserName = gameWeekResult.getTeam().getName();
        this.topUserFplName = gameWeekResult.getTeam().getFplName();
        this.data = gameWeekResult.getLocalPoint();
    }
}
