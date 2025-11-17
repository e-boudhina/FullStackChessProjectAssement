package com.elyes.chess.websocket;

public class InviteMessage {

    private Long fromUserId;
    private Long toUserId;

    public InviteMessage() {}

    public InviteMessage(Long fromUserId, Long toUserId) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
    }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
}
