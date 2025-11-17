package com.elyes.chess.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEvents {
	// This services removes the user once he disconnects from the list of online users 
    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEvents(OnlineUserService onlineUserService, SimpMessagingTemplate messagingTemplate) {
        this.onlineUserService = onlineUserService;
        this.messagingTemplate = messagingTemplate;
    }

    //Listens to Spring events like “a WebSocket session has disconnected”.
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        
        //Remove that user from onlineUsers map.
        onlineUserService.removeUser(sessionId);
        
        //Broadcast the updated list again.
        messagingTemplate.convertAndSend("/topic/online-users", onlineUserService.getOnlineUsers());
    }
}