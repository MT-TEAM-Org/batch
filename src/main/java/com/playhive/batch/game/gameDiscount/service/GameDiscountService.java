package com.playhive.batch.game.gameDiscount.service;

import com.playhive.batch.game.gameDiscount.dto.GameDiscountSaveRequest;
import com.playhive.batch.game.gameDiscount.repository.GameDiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GameDiscountService {

    private final GameDiscountRepository gameDiscountRepository;

    public void saveGameDiscount(GameDiscountSaveRequest gameDiscountSaveRequest) {
        gameDiscountRepository.save(gameDiscountSaveRequest.toEntity());
    }
}