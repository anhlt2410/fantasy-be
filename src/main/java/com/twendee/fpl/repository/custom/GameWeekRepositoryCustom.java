package com.twendee.fpl.repository.custom;

import com.twendee.fpl.model.GameWeekResult;
import com.twendee.fpl.model.Team;

import java.util.List;

public interface GameWeekRepositoryCustom {
    GameWeekResult findByGameWeekAndTeamId(Integer gameWeek, Long teamId);

    List<GameWeekResult> findByTeamAndGameWeekLessThan(Team team, Integer gameWeek);
}
