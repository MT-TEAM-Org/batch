package com.playhive.batch.game.gameDiscount.repository;

import com.playhive.batch.game.gameDiscount.entity.GameDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameDiscountRepository extends JpaRepository<GameDiscount, Long> {
}