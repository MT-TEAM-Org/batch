package com.playhive.batch.match.match.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.match.match.domain.Match;
import com.playhive.batch.match.match.repository.MatchRepository;
import com.playhive.batch.match.team.domain.Team;
import com.playhive.batch.match.team.domain.TeamCategory;
import com.playhive.batch.match.team.repository.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchReadService {

	private final MatchRepository matchRepository;

	public LocalDateTime getLatestMatchStartTime() {
		return matchRepository.findTopByOrderByStartTimeDesc()
			.map(Match::getStartTime)
			.orElse(null); // 또는 원하는 기본값 반환
	}
}
