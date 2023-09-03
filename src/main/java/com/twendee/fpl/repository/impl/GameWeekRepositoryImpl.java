package com.twendee.fpl.repository.impl;

import com.twendee.fpl.model.GameWeekResult;
import com.twendee.fpl.model.Team;
import com.twendee.fpl.repository.custom.GameWeekRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class GameWeekRepositoryImpl implements GameWeekRepositoryCustom {

    @Autowired
    EntityManager entityManager;

    @Override
    public GameWeekResult findByGameWeekAndTeamId(Integer gameWeek, Long teamId) {
        Query query
                = entityManager.createQuery(
                "SELECT g FROM GameWeekResult g WHERE g.gameWeek = :gameWeek and g.team.fplId = :teamId");
        query.setParameter("gameWeek", gameWeek).setParameter("teamId", teamId);
        return (GameWeekResult) query.getSingleResult();
    }

    @Override
    public List<GameWeekResult> findByTeamAndGameWeekLessThan(Team team, Integer gameWeek) {
        Query query
                = entityManager.createQuery(
                "SELECT g FROM GameWeekResult g WHERE g.gameWeek <= :gameWeek and g.team = :team");
        query.setParameter("gameWeek", gameWeek).setParameter("team", team);
        return query.getResultList();
    }
}
