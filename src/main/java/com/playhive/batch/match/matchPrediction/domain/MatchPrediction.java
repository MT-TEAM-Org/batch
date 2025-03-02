package com.playhive.batch.match.matchPrediction.domain;

import com.playhive.batch.global.domain.BaseTime;
import com.playhive.batch.match.match.domain.Match;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "p_match_prediction")
public class MatchPrediction extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne(fetch = FetchType.LAZY)
	private Match match;
	private int home;
	private int away;

	@Builder
	public MatchPrediction(Long id, Match match, int home, int away) {
		this.id = id;
		this.match = match;
		this.home = home;
		this.away = away;
	}

	public static MatchPrediction createEntity(Match match) {
		return MatchPrediction.builder()
			.match(match)
			.build();
	}
}
