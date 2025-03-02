package com.playhive.batch.match.team.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.match.team.dto.service.request.TeamServiceRequest;
import com.playhive.batch.match.team.repository.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

	private final TeamRepository teamRepository;

	public void save(TeamServiceRequest teamServiceRequest) {
		if (!teamRepository.existsByNameAndCategory(teamServiceRequest.getName(), teamServiceRequest.getCategory())) {
			teamRepository.save((teamServiceRequest.toEntity()));
		}
	}
}
