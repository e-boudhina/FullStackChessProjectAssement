package com.elyes.chess.websocket;

import com.elyes.chess.auth.AuthService;
import com.elyes.chess.user.User;
import com.elyes.chess.user.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class PresenceController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceController(AuthService authService,
                              UserRepository userRepository,
                              OnlineUserService onlineUserService,
                              SimpMessagingTemplate messagingTemplate) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.onlineUserService = onlineUserService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/register-user")
    public void registerUser(@Payload UserRegisterMessage message, SimpMessageHeaderAccessor headerAccessor) {
    	
    	/*
    	 * each session is like a tab that user has opened by tracking it we track multiple tabs and remove the correct connection once a user disconnects
    	 * When a browser closes the tab or loses internet, spring triggers the "SessionDisconnectEvent event" which contains ONLY the session id and nothing else:
    	 * If we didn't store (sessionId → user), we would NOT know which user went offline!
    	 * Moreover,Later when I implement “invitation”: When a Player A sends an invite to Player B, Backend needs to send a message only to player B’s session
    	 */
        String sessionId = headerAccessor.getSessionId();
        
        // Find who the user is based on this token
        Long userId = authService.getUserIdFromToken(message.getToken());
        if (userId == null) {
            return; // invalid token, ignore (User does not exist or expired token)
        }

        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return;
        }

        User user = optUser.get();
        
        // Fetch user's username from the User ojbect
        OnlineUserDto dto = new OnlineUserDto(user.getId(), user.getUsername());
        
        // Saves the session id and user (id + username) to the online list of user 
        onlineUserService.addUser(sessionId, dto);

        // broadcast updated list to all clients
        messagingTemplate.convertAndSend("/topic/online-users", onlineUserService.getOnlineUsers());
    }
}
