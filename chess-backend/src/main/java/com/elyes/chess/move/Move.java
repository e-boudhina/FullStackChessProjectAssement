package com.elyes.chess.move;

import jakarta.persistence.*;

import java.time.Instant;

import com.elyes.chess.game.Game;

@Entity
@Table(name = "moves")
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(nullable = false, length = 2)
    private String fromSquare;

    @Column(nullable = false, length = 2)
    private String toSquare;   

    @Column(nullable = false)
    private int moveIndex;     // 1,2,3,... order of the moves

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // optional: store board state after this move (simplifies resume)
    private String fen;

    public Move() {}

    public Move(Game game, String fromSquare, String toSquare, int moveIndex) {
        this.game = game;
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.moveIndex = moveIndex;
    }

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public String getFromSquare() { return fromSquare; }
    public void setFromSquare(String fromSquare) { this.fromSquare = fromSquare; }

    public String getToSquare() { return toSquare; }
    public void setToSquare(String toSquare) { this.toSquare = toSquare; }

    public int getMoveIndex() { return moveIndex; }
    public void setMoveIndex(int moveIndex) { this.moveIndex = moveIndex; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getFen() { return fen; }
    public void setFen(String fen) { this.fen = fen; }
}
