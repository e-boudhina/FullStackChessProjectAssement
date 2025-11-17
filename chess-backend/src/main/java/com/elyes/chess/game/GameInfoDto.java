package com.elyes.chess.game;

public class GameInfoDto {
    private Long id;
    private Long whitePlayerId;
    private Long blackPlayerId;
    private String whiteUsername;
    private String blackUsername;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWhitePlayerId() { return whitePlayerId; }
    public void setWhitePlayerId(Long whitePlayerId) { this.whitePlayerId = whitePlayerId; }

    public Long getBlackPlayerId() { return blackPlayerId; }
    public void setBlackPlayerId(Long blackPlayerId) { this.blackPlayerId = blackPlayerId; }

    public String getWhiteUsername() { return whiteUsername; }
    public void setWhiteUsername(String whiteUsername) { this.whiteUsername = whiteUsername; }

    public String getBlackUsername() { return blackUsername; }
    public void setBlackUsername(String blackUsername) { this.blackUsername = blackUsername; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}