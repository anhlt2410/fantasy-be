package com.twendee.fpl.repository.impl;

import com.twendee.fpl.model.GameWeekResult;
import com.twendee.fpl.model.Team;
import com.twendee.fpl.repository.custom.GameWeekRepositoryCustom;
import com.twendee.fpl.repository.custom.TeamRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class TeamRepositoryImpl implements TeamRepositoryCustom {

    @Autowired
    EntityManager entityManager;

    @Override
    public List<Team> findTop3ByOrderByPositionAsc() {
        Query query
                = entityManager.createQuery(
                "SELECT t FROM Team t ORDER BY t.position ASC");
        query.setMaxResults(3);
        return query.getResultList();
    }
}
