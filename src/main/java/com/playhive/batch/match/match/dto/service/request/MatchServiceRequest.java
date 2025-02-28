package com.playhive.batch.match.match.dto.service.request;

import java.time.LocalDateTime;

import com.playhive.batch.match.match.domain.Match;
import com.playhive.batch.match.match.domain.MatchCategory;
import com.playhive.batch.match.team.domain.Team;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MatchServiceRequest {
	private String homeTeamName;
	private String homeTeamLogo;
	private String awayTeamName;
	private String awayTeamLogo;
	private String place;
	private MatchCategory category;
	private LocalDateTime startTime;

	@Builder
	public MatchServiceRequest(String homeTeamName, String homeTeamLogo, String awayTeamName, String awayTeamLogo,
		String place, MatchCategory category,
		LocalDateTime startTime) {
		this.homeTeamName = homeTeamName;
		this.homeTeamLogo = homeTeamLogo;
		this.awayTeamName = awayTeamName;
		this.awayTeamLogo = awayTeamLogo;
		this.place = place;
		this.category = category;
		this.startTime = startTime;
	}

	public Match toEntity(Team homeTeam, Team awayTeam) {
		return Match.builder()
			.homeTeam(homeTeam)
			.awayTeam(awayTeam)
			.place(place)
			.category(category)
			.startTime(startTime)
			.build();
	}

	public static MatchServiceRequest createRequest(String homeTeamName, String homeTeamLogo,
		String awayTeamName, String awayTeamLogo, String place, MatchCategory category,
		LocalDateTime startTime) {
		return MatchServiceRequest.builder()
			.homeTeamName(homeTeamName)
			.homeTeamLogo(homeTeamLogo)
			.awayTeamName(awayTeamName)
			.awayTeamLogo(awayTeamLogo)
			.place(place)
			.category(category)
			.startTime(startTime)
			.build();
	}

}
