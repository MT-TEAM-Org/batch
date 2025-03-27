package com.playhive.batch.match.match.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.match.match.domain.Match;
import com.playhive.batch.match.match.dto.service.request.MatchServiceRequest;
import com.playhive.batch.match.match.repository.MatchRepository;
import com.playhive.batch.match.matchPrediction.service.MatchPredictionService;
import com.playhive.batch.match.team.domain.Team;
import com.playhive.batch.match.team.domain.TeamCategory;
import com.playhive.batch.match.team.repository.TeamRepository;
import com.playhive.batch.match.team.service.TeamReadService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {

	private final MatchRepository matchRepository;
	private final TeamReadService teamReadService;
	private final TeamRepository teamRepository;
	private final MatchPredictionService matchPredictionService;

	public void save(MatchServiceRequest matchServiceRequest) {
		TeamCategory teamCategory = TeamCategory.fromText(matchServiceRequest.getCategory().getText());

		Team homeTeam = saveTeam(matchServiceRequest.getHomeTeamName(), matchServiceRequest.getHomeTeamLogo(),
			teamCategory);
		Team awayTeam = saveTeam(matchServiceRequest.getAwayTeamName(), matchServiceRequest.getHomeTeamLogo(),
			teamCategory);

		Match match = matchRepository.save(matchServiceRequest.toEntity(homeTeam, awayTeam));
		matchPredictionService.save(match);
	}

	public Team saveTeam(String name, String logo, TeamCategory teamCategory) {
		Team team = teamReadService.findByNameAndCategory(name, teamCategory);
		if (team == null) {
			return teamRepository.save(Team.createEntity(name, logo, teamCategory));
		}
		team.updateLogo(logo);
		return team;
	}
}
