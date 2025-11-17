package com.elyes.chess.game;

import com.elyes.chess.common.GameStatus;
import com.elyes.chess.move.Move;
import com.elyes.chess.move.MoveRepository;
import com.elyes.chess.user.User;
import com.elyes.chess.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final MoveRepository moveRepository;

    public GameService(GameRepository gameRepository,
                       UserRepository userRepository,
                       MoveRepository moveRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.moveRepository = moveRepository;
    }

    /**
     * Create a new game between two players.
     */
    public Game createGame(Long whitePlayerId, Long blackPlayerId) {
        User white = userRepository.findById(whitePlayerId)
                .orElseThrow(() -> new RuntimeException("White player not found"));
        User black = userRepository.findById(blackPlayerId)
                .orElseThrow(() -> new RuntimeException("Black player not found"));

        Game game = new Game(white, black);
        game.setStatus(GameStatus.ACTIVE);
        return gameRepository.save(game);
    }

    /**
     * Record a move for a given game and persist it.
     */
    @Transactional
    public Move recordMove(Long gameId, String from, String to, Long playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        int moveIndex = moveRepository.countByGame(game); // 0,1,2,...

        Move move = new Move();
        move.setGame(game);
        move.setFromSquare(from);
        move.setToSquare(to);
        move.setMoveIndex(moveIndex);
        move.setCreatedAt(Instant.now());
        // move.setFen(null);

        return moveRepository.save(move);
    }

    /**
     * Get all moves of a game in order (useful for resume/replay).
     */
    public List<Move> getMovesForGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        return moveRepository.findByGameOrderByMoveIndexAsc(game);
    }
    
    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }
}
