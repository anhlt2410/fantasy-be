package com.twendee.fpl.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PairH2HDTO {
    private String team1Name;
    private Long team1fplId;
    private String team1fplName;
    private Integer team1Point;
    private String team2Name;
    private Long team2fplId;
    private String team2fplName;
    private Integer team2Point;
}
