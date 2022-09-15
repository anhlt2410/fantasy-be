package com.twendee.fpl.dto;

import com.twendee.fpl.model.GameWeekResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopDTO {
    private String topUserName;
    private String topUserFplName;
    private Integer data;

    public TopDTO(GameWeekResult gameWeekResult) {
        this.topUserName = gameWeekResult.getTeam().getName();
        this.topUserFplName = gameWeekResult.getTeam().getFplName();
        this.data = gameWeekResult.getLocalPoint();
    }
}
