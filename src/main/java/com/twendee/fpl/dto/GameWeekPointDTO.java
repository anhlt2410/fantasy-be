package com.twendee.fpl.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GameWeekPointDTO {
    private Long teamId;
    private Integer gameWeek;
    private Integer point;
    private Integer transfer;
    private Integer minusPoints;
}
