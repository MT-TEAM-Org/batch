package com.playhive.batch.match.match.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.playhive.batch.match.match.domain.Match;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
	Optional<Match> findTopByOrderByStartTimeDesc();
}
