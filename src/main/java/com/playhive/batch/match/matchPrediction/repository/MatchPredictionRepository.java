package com.playhive.batch.match.matchPrediction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.playhive.batch.match.matchPrediction.domain.MatchPrediction;

@Repository
public interface MatchPredictionRepository extends JpaRepository<MatchPrediction, Long> {
}
