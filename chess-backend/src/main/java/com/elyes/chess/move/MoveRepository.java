package com.elyes.chess.move;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elyes.chess.game.Game;

import java.util.List;

public interface MoveRepository extends JpaRepository<Move, Long> {
    int countByGame(Game game);
    List<Move> findByGameOrderByMoveIndexAsc(Game game);
}
