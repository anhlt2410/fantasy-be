package com.twendee.fpl.repository;

import com.twendee.fpl.model.Team;
import com.twendee.fpl.repository.custom.TeamRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>, TeamRepositoryCustom {
    Team findByFplId(Long fplId);

    List<Team> findAllByOrderByPositionAsc();

    @Query(value = "SELECT t FROM Team t WHERE t.gameWeekWinnerReward > 0 ORDER BY t.gameWeekWinnerReward DESC")
    List<Team> findTop1Winners();

    Team findFirstByOrderByMoneyAsc();
}
