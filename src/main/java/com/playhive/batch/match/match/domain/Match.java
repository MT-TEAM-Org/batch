package com.playhive.batch.match.match.domain;

import java.time.LocalDateTime;

import com.playhive.batch.global.domain.BaseTime;
import com.playhive.batch.match.team.domain.Team;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "p_match")
public class Match extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Team homeTeam;

	@ManyToOne(fetch = FetchType.LAZY)
	private Team awayTeam;

	private String place;

	private String leagueName;

	@Enumerated(EnumType.STRING)
	private MatchCategory category;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	@Builder
	public Match(Long id, Team homeTeam, Team awayTeam, String place, String leagueName, MatchCategory category, LocalDateTime startTime, LocalDateTime endTime) {
		this.id = id;
		this.homeTeam = homeTeam;
		this.awayTeam = awayTeam;
		this.place = place;
		this.leagueName = leagueName;
		this.category = category;
		this.startTime = startTime;
		this.endTime = endTime;
	}
}
