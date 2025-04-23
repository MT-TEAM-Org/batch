package com.playhive.batch.match.matchPrediction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.match.match.domain.Match;
import com.playhive.batch.match.matchPrediction.domain.MatchPrediction;
import com.playhive.batch.match.matchPrediction.repository.MatchPredictionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchPredictionService {

	private final MatchPredictionRepository matchPredictionRepository;

	public void save(Match match) {
		matchPredictionRepository.save(MatchPrediction.createEntity(match));
	}

}
