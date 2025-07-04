package com.playhive.batch.match.match.repository;

import com.playhive.batch.match.match.domain.Match;
import com.playhive.batch.match.team.domain.Team;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    boolean existsByHomeTeamAndAwayTeamAndStartTime(Team homeTeam, Team awayTeam, LocalDateTime startTime);

    Optional<Match> findTopByOrderByStartTimeDesc();
}
