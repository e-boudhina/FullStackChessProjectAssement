package com.elyes.chess.websocket;

import com.elyes.chess.game.GameService;
import com.elyes.chess.websocket.dto.GameMoveMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameMoveWebSocketController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameMoveWebSocketController(GameService gameService,
                                       SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/game/move") // client sends to /app/game/move
    public void handleMove(GameMoveMessage message) {
        // persist move
        gameService.recordMove(
                message.getGameId(),
                message.getFromSquare(),
                message.getToSquare(),
                message.getPlayerId()
        );

        // broadcast to both players
        String destination = "/topic/game." + message.getGameId();
        messagingTemplate.convertAndSend(destination, message);
    }
}
