package com.twendee.fpl.repository;

import com.twendee.fpl.model.GameWeekResult;
import com.twendee.fpl.model.Team;
import com.twendee.fpl.repository.custom.GameWeekRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameWeekResultRepository extends JpaRepository<GameWeekResult, Long>, GameWeekRepositoryCustom {

    List<GameWeekResult> findByGameWeek(int gameWeek);

    GameWeekResult findByGameWeekAndTeam(int gameWeek, Team team);

    List<GameWeekResult> findByGameWeekOrderByPositionAsc(int gameWeek);

    @Query(value = "SELECT max(gameWeek) FROM GameWeekResult")
    Integer getMaxGameWeek();


}
