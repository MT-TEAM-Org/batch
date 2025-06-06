package com.playhive.batch.match.team.domain;

import com.playhive.batch.global.domain.BaseTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "p_team")
public class Team extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String name;

	private String logo;

	@Enumerated(EnumType.STRING)
	private TeamCategory category;

	@Builder
	public Team(int id, String name, String logo, TeamCategory category) {
		this.id = id;
		this.name = name;
		this.logo = logo;
		this.category = category;
	}

	public void updateLogo(String logo) {
		if (this.logo == null) {
			this.logo = logo;
		}
	}

	public static Team createEntity(String name, String logo, TeamCategory category) {
		return Team.builder()
			.name(name)
			.logo(logo)
			.category(category)
			.build();
	}
}
