package com.elyes.chess.websocket;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/lobby")
@CrossOrigin(origins = "*")
public class LobbyRestController {

    private final OnlineUserService onlineUserService;

    public LobbyRestController(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    @GetMapping("/online-users")
    public Collection<OnlineUserDto> getOnlineUsers() {
        return onlineUserService.getOnlineUsers();
    }
}