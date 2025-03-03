package com.playhive.batch.game.gameEvent.repository;

import com.playhive.batch.game.gameEvent.entity.GameEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameEventRepository extends JpaRepository<GameEvent, Long> {
}