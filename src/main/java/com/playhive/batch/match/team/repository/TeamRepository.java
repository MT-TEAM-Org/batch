package com.playhive.batch.match.team.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.playhive.batch.match.team.domain.Team;
import com.playhive.batch.match.team.domain.TeamCategory;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {
	boolean existsByNameAndCategory(String name, TeamCategory category);
	Optional<Team> findByNameAndCategory(String name, TeamCategory category);
}
