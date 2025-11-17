package com.elyes.chess.game;

import com.elyes.chess.move.MoveRepository;
import com.elyes.chess.websocket.dto.GameMoveMessage;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*")
public class GameRestController {

    private final GameService gameService;

    public GameRestController(GameService gameService) {
        this.gameService = gameService;
    }

    // basic game info (who is white / who is black)
    @GetMapping("/{gameId}")
    public GameInfoDto getGame(@PathVariable Long gameId) {
        Game game = gameService.getGame(gameId);

        GameInfoDto dto = new GameInfoDto();
        dto.setId(game.getId());
        dto.setWhitePlayerId(game.getWhitePlayer().getId());
        dto.setBlackPlayerId(game.getBlackPlayer().getId());
        dto.setWhiteUsername(game.getWhitePlayer().getUsername());
        dto.setBlackUsername(game.getBlackPlayer().getUsername());
        dto.setStatus(game.getStatus().name());
        return dto;
    }

    // Same route as before, but now via GameService
    @GetMapping("/{gameId}/moves")
    public List<GameMoveMessage> getMoves(@PathVariable Long gameId) {
        return gameService.getMovesForGame(gameId).stream()
                .map(m -> {
                    GameMoveMessage dto = new GameMoveMessage();
                    dto.setGameId(gameId);
                    dto.setFromSquare(m.getFromSquare());
                    dto.setToSquare(m.getToSquare());
                    dto.setPlayerId(null); //
                    return dto;
                })
                .toList();
    }
}