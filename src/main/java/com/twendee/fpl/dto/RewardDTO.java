package com.twendee.fpl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardDTO {
	List<TopDTO> top3LeaguePoint;
	List<TopDTO> topGWPoint;
	TopDTO topDonate;
	List<TopDTO> gameWeekWinnerReward;
	List<TopDTO> top3H2H;
}
