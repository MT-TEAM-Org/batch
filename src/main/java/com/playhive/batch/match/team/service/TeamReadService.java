package com.playhive.batch.match.team.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.match.team.domain.Team;
import com.playhive.batch.match.team.domain.TeamCategory;
import com.playhive.batch.match.team.dto.service.request.TeamServiceRequest;
import com.playhive.batch.match.team.repository.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamReadService {

	private final TeamRepository teamRepository;

	public Team findByNameAndCategory(String name, TeamCategory category) {
		return teamRepository.findByNameAndCategory(name, category)
			.orElse(null);
	}
}
