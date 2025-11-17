import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GameMove, Websocket } from '../../services/websocket';
import { Auth, AuthResponse } from '../../services/auth';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';

type Piece = string | null; // e.g. 'wp', 'bp', 'wk', etc.

export interface GameInfo {
  id: number;
  whitePlayerId: number;
  blackPlayerId: number;
  whiteUsername: string;
  blackUsername: string;
  status: string;
}

@Component({
  selector: 'app-game',
  imports: [CommonModule],
  templateUrl: './game.html',
  styleUrl: './game.css',
})
export class Game implements OnInit {

  gameId!: number;

  // 8x8 board: [row][col], row 0 = black side, row 7 = white side
  board: Piece[][] = [];

  // track selection
  selectedRow: number | null = null;
  selectedCol: number | null = null;

  // map piece code -> unicode symbol
  pieceSymbols: Record<string, string> = {
    'wp': '♙', 'wr': '♖', 'wn': '♘', 'wb': '♗', 'wq': '♕', 'wk': '♔',
    'bp': '♟', 'br': '♜', 'bn': '♞', 'bb': '♝', 'bq': '♛', 'bk': '♚'
  };

  currentUser!: AuthResponse;
  private subs: Subscription[] = [];

  //Advanced
  myColor: 'white' | 'black' = 'white';
  opponentName = '';

  rowIndices: number[] = [0,1,2,3,4,5,6,7];
  colIndices: number[] = [0,1,2,3,4,5,6,7];

  //Movment sound
  private moveAudio = new Audio('sounds/movement.mp3');

  private playMoveSound(): void {
  try {
    this.moveAudio.currentTime = 0; // rewind so rapid moves still play
    this.moveAudio.play();
    console.log("sound played");
  } catch (e) {
    console.warn('Could not play move sound', e);
  }
}

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ws: Websocket,
    private auth: Auth,
    private http: HttpClient
  ) {}

  // ----------------- lifecycle -----------------

  ngOnInit(): void {
      window.addEventListener(
    'click',
    () => {
      this.moveAudio.play().catch(() => {});
    },
    { once: true }
  );
    // game id from route
    this.gameId = Number(this.route.snapshot.paramMap.get('id'));

    // ensure we have a logged-in user
    const session = this.auth.getSession();
    if (!session) {
      this.router.navigateByUrl('/login');
      return;
    }
    this.currentUser = session;

    // init starting position
    this.initBoard();

      // get game info: who is white/black?
  this.http.get<GameInfo>(`http://localhost:8075/api/games/${this.gameId}`)
    .subscribe(info => {
      if (info.whitePlayerId === this.currentUser.userId) {
        this.myColor = 'white';
        this.opponentName = info.blackUsername;
      } else if (info.blackPlayerId === this.currentUser.userId) {
        this.myColor = 'black';
        this.opponentName = info.whiteUsername;
      } else {
        // not one of the players -> default
        this.myColor = 'white';
        this.opponentName = info.whiteUsername;
      }

      this.setupBoardOrientation();
    });


    // ensure WebSocket is connected
    this.ws.connect(this.currentUser);

    // subscribe to this specific game's topic
    this.ws.subscribeToGame(this.gameId);

    // 1) load existing moves from backend (resume)
    this.http
      .get<GameMove[]>(`http://localhost:8075/api/games/${this.gameId}/moves`)
      .subscribe(moves => {
        moves.forEach(m => this.applyMoveOnBoard(m));
      });

    // 2) listen for live moves from WebSocket
    this.subs.push(
      this.ws.gameMoves$.subscribe(move => {
        // ignore our own moves (we already applied them when sending)
        if (move.playerId === this.currentUser.userId) {
          return;
        }
        if (move.gameId !== this.gameId) {
          return;
        }
        this.applyMoveOnBoard(move);

        // 🔊 play sound for opponent’s move
        this.playMoveSound();
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  // ----------------- board helpers -----------------

  private initBoard(): void {
    this.board = [
      ['br','bn','bb','bq','bk','bb','bn','br'],
      ['bp','bp','bp','bp','bp','bp','bp','bp'],
      [null,null,null,null,null,null,null,null],
      [null,null,null,null,null,null,null,null],
      [null,null,null,null,null,null,null,null],
      [null,null,null,null,null,null,null,null],
      ['wp','wp','wp','wp','wp','wp','wp','wp'],
      ['wr','wn','wb','wq','wk','wb','wn','wr']
    ];
  }

  getPieceSymbol(piece: Piece): string {
    return piece ? this.pieceSymbols[piece] ?? '' : '';
  }

  isSelected(row: number, col: number): boolean {
    return this.selectedRow === row && this.selectedCol === col;
  }

  // convert row/col <-> chess square (e2, e4, etc.)
  private coordsToSquare(row: number, col: number): string {
    const file = String.fromCharCode('a'.charCodeAt(0) + col); // 0→a, 1→b...
    const rank = 8 - row;                                     // row 0→8
    return `${file}${rank}`;
  }

  private squareToCoords(square: string): { row: number; col: number } {
    const file = square[0].toLowerCase();
    const rank = Number(square[1]);

    const col = file.charCodeAt(0) - 'a'.charCodeAt(0);
    const row = 8 - rank;

    return { row, col };
  }

  // apply a move object onto the board
  private applyMoveOnBoard(move: GameMove): void {
    const from = this.squareToCoords(move.fromSquare);
    const to = this.squareToCoords(move.toSquare);

    const piece = this.board[from.row][from.col];
    this.board[to.row][to.col] = piece;
    this.board[from.row][from.col] = null;
  }

  // ----------------- click handler -----------------

  onCellClick(row: number, col: number): void {
    const clicked = this.board[row][col];

    // nothing selected yet
    if (this.selectedRow === null || this.selectedCol === null) {
      if (!clicked) return; // empty square

      // 🔹 allow ONLY selecting your own pieces
      const pieceColor = clicked[0]; // 'w' or 'b'
      if (this.myColor === 'white' && pieceColor !== 'w') {
        return; // can't select black piece if you're white
      }
      if (this.myColor === 'black' && pieceColor !== 'b') {
        return; // can't select white piece if you're black
      }

      this.selectedRow = row;
      this.selectedCol = col;
      return;
    }

    // clicking same cell → unselect
    if (this.selectedRow === row && this.selectedCol === col) {
      this.selectedRow = this.selectedCol = null;
      return;
    }

    // compute chess squares
    const fromSquare = this.coordsToSquare(this.selectedRow, this.selectedCol);
    const toSquare = this.coordsToSquare(row, col);

    // 1 - apply move locally
    const fromPiece = this.board[this.selectedRow][this.selectedCol];
    this.board[row][col] = fromPiece;
    this.board[this.selectedRow][this.selectedCol] = null;

    // 🔊 play sound for your move
    this.playMoveSound();

    // 2 - send move over WebSocket (this will persist in DB + send to opponent)
    const move: GameMove = {
      gameId: this.gameId,
      playerId: this.currentUser.userId,
      fromSquare,
      toSquare
    };
    this.ws.sendMove(move);

    // clear selection
    this.selectedRow = this.selectedCol = null;
  }

  //Board orientation
  private setupBoardOrientation(): void {
  if (this.myColor === 'white') {
    this.rowIndices = [0,1,2,3,4,5,6,7];
    this.colIndices = [0,1,2,3,4,5,6,7];
  } else {
    // black sees board from the opposite side
    this.rowIndices = [7,6,5,4,3,2,1,0];
    this.colIndices = [7,6,5,4,3,2,1,0];
  }
}
}
