package com.twendee.fpl.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FullGameWeekResultDTO {
    private List<GameWeekResultDTO> gameWeekResultDTOList = new ArrayList<>();
    private List<PairH2HDTO> h2HDTOList = new ArrayList<>();
}
