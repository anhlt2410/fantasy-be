package com.twendee.fpl.repository.custom;

import com.twendee.fpl.model.Team;

import java.util.List;

public interface TeamRepositoryCustom {
	List<Team> findTop3ByOrderByPositionAsc();
}
