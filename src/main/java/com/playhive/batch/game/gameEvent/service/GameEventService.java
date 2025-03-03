package com.playhive.batch.game.gameEvent.service;

import com.playhive.batch.game.gameEvent.dto.GameEventSaveRequest;
import com.playhive.batch.game.gameEvent.repository.GameEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GameEventService {

    private final GameEventRepository gameEventRepository;

    public void saveGameEvent(GameEventSaveRequest gameEventSaveRequest) {
        gameEventRepository.save(gameEventSaveRequest.toEntity());
    }
}