package com.elyes.chess.websocket;

public class InviteNotification {

    public enum Type {
        INVITE,
        ACCEPT,
        REFUSE
    }

    private Type type;
    private Long fromUserId;
    private String fromUsername;
    private Long toUserId;
    private Long gameId; // only set on ACCEPT

    public InviteNotification() {}

    public InviteNotification(Type type, Long fromUserId, String fromUsername,
                              Long toUserId, Long gameId) {
        this.type = type;
        this.fromUserId = fromUserId;
        this.fromUsername = fromUsername;
        this.toUserId = toUserId;
        this.gameId = gameId;
    }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String fromUsername) { this.fromUsername = fromUsername; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }
}
