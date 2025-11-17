package com.elyes.chess.websocket;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    // sessionId -> OnlineUserDto
    private final Map<String, OnlineUserDto> onlineUsers = new ConcurrentHashMap<>();

    // Add the user to the list of online user once he said is "token"
    public void addUser(String sessionId, OnlineUserDto user) {
        onlineUsers.put(sessionId, user);
    }

    // Removes the user from online list of user
    public void removeUser(String sessionId) {
        onlineUsers.remove(sessionId);
    }
    
    //Returns all online user
    public Collection<OnlineUserDto> getOnlineUsers() {
        return onlineUsers.values();
    }
}
