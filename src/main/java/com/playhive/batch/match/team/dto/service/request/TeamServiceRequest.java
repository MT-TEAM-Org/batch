package com.playhive.batch.match.team.dto.service.request;

import com.playhive.batch.match.team.domain.Team;
import com.playhive.batch.match.team.domain.TeamCategory;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamServiceRequest {
	private String name;
	private String logo;
	private TeamCategory category;

	@Builder
	public TeamServiceRequest(String name, String logo, TeamCategory category) {
		this.name = name;
		this.logo = logo;
		this.category = category;
	}

	public Team toEntity() {
		return Team.builder()
			.name(name)
			.logo(logo)
			.category(category)
			.build();
	}

	public static TeamServiceRequest createRequest(String name, String logo, TeamCategory category) {
		return TeamServiceRequest.builder()
			.name(name)
			.logo(logo)
			.category(category)
			.build();
	}
}
