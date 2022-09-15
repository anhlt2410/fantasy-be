package com.twendee.fpl.repository;

import com.twendee.fpl.dto.TopDTO;
import com.twendee.fpl.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Team findByFplId(Long fplId);

    List<Team> findAllByOrderByPositionAsc();

    Team findFirstByOrderByMoneyAsc();
}
