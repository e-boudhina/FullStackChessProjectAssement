import { Injectable, NgZone } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';

import { BehaviorSubject, Subject } from 'rxjs';
import { AuthResponse } from './auth';


export interface OnlineUser {
  userId: number;
  username: string;
}

export type InviteType = 'INVITE' | 'ACCEPT' | 'REFUSE';

export interface InviteNotification {
  type: InviteType;
  fromUserId: number;
  fromUsername: string;
  toUserId: number;
  gameId?: number | null;
}

export interface GameMove {
  gameId: number;
  playerId: number;
  fromSquare: string; // "e2"
  toSquare: string;   // "e4"
}

@Injectable({
  providedIn: 'root',
})
export class Websocket {

  private client: Client | null = null;
  private session: AuthResponse | null = null;

  private onlineUsersSubject = new BehaviorSubject<OnlineUser[]>([]);
  onlineUsers$ = this.onlineUsersSubject.asObservable();

  private inviteSubject = new Subject<InviteNotification>();
  invites$ = this.inviteSubject.asObservable();

  private gameMoveSubject = new Subject<GameMove>();
  gameMoves$ = this.gameMoveSubject.asObservable();

  private subscribedGameIds = new Set<number>();

  constructor(private zone: NgZone) {}

  connect(session: AuthResponse): void {
    if (this.client && this.client.connected) {
      return; // already connected
    }

    this.session = session;

    this.client = new Client({
      brokerURL: 'ws://localhost:8075/ws',   // native WebSocket URL
      reconnectDelay: 5000,                  // try to reconnect every 5s
      debug: (msg) => console.log('[STOMP]', msg)
    });

    this.client.onConnect = () => {
      console.log('STOMP connected');

      const myId = session.userId;

      // 1- SUBSCRIBE FIRST (never miss events)
      this.client!.subscribe('/topic/online-users', msg => {
        const users = JSON.parse(msg.body);
        this.zone.run(() => this.onlineUsersSubject.next(users));
      });

      this.client!.subscribe(`/topic/invitations.${myId}`, msg => {
        const invite = JSON.parse(msg.body);
        this.zone.run(() => this.inviteSubject.next(invite));
      });

      // 🔁 1bis - re-subscribe to all game topics we care about
      for (const gameId of this.subscribedGameIds) {
        this.doSubscribeToGame(gameId);
      }

      // 2- ONLY THEN REGISTER
      this.client!.publish({
        destination: '/app/register-user',
        body: JSON.stringify({ token: session.token })
      });
    };


    this.client.onStompError = (frame) => {
      console.error('STOMP error', frame.headers['message'], frame.body);
    };

    this.client.activate();
  }



  disconnect(): void {
    this.client?.deactivate();
    this.client = null;
    this.session = null;
    this.onlineUsersSubject.next([]);
  }

  sendInvite(toUserId: number): void {
    if (!this.client || !this.client.connected || !this.session) {
      return;
    }
    this.client.publish({
      destination: '/app/invite',
      body: JSON.stringify({
        fromUserId: this.session.userId,
        toUserId
      })
    });
  }

  respondToInvite(fromUserId: number, accepted: boolean): void {
    if (!this.client || !this.client.connected || !this.session) {
      return;
    }
    // here fromUserId = invited player, toUserId = original inviter
    this.client.publish({
      destination: '/app/invite-response',
      body: JSON.stringify({
        fromUserId: this.session.userId,
        toUserId: fromUserId,
        accepted
      })
    });
  }

  //Part 2
subscribeToGame(gameId: number): void {
  // remember that we care about this game
  this.subscribedGameIds.add(gameId);

  // if client is already connected, subscribe right now
  if (this.client && this.client.connected) {
    this.doSubscribeToGame(gameId);
  }
}
private doSubscribeToGame(gameId: number): void {
  if (!this.client) {
    return;
  }

  this.client.subscribe(`/topic/game.${gameId}`, msg => {
    const move = JSON.parse(msg.body) as GameMove;
    console.log('RECEIVED GAME MOVE', move);

    this.zone.run(() => {
      this.gameMoveSubject.next(move);
    });
  });
}

sendMove(move: GameMove): void {
  if (!this.client || !this.client.connected) return;

  this.client.publish({
    destination: '/app/game/move',
    body: JSON.stringify(move)
  });
}

}

