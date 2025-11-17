package com.elyes.chess.websocket;


public class UserRegisterMessage {
	
	
    private String token; // token from /auth/login or /auth/register => What the client sends on connect ({ "token": "d2b3-9104-..." })

    public UserRegisterMessage() {}

    public UserRegisterMessage(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
