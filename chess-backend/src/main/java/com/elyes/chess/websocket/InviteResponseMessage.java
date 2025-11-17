package com.elyes.chess.websocket;

public class InviteResponseMessage {

    private Long fromUserId; // the invited player responding
    private Long toUserId;   // original inviter
    private boolean accepted;

    public InviteResponseMessage() {}

    public InviteResponseMessage(Long fromUserId, Long toUserId, boolean accepted) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.accepted = accepted;
    }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
}