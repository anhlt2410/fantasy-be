package com.twendee.fpl.repository.custom;

import com.twendee.fpl.dto.TopDTO;
import com.twendee.fpl.model.GameWeekResult;
import com.twendee.fpl.model.Team;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface GameWeekRepositoryCustom {
    GameWeekResult findByGameWeekAndTeam(Integer gameWeek, Team team);

    List<GameWeekResult> findByTeamAndGameWeekLessThan(Team team, Integer gameWeek);
}
