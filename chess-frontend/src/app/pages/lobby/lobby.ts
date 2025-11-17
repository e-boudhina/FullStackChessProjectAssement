import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { InviteNotification, OnlineUser, Websocket } from '../../services/websocket';
import { Auth, AuthResponse } from '../../services/auth';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-lobby',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './lobby.html',
  styleUrl: './lobby.css',
})
export class Lobby implements OnInit, OnDestroy {

  currentUser: AuthResponse | null = null;
  onlineUsers: OnlineUser[] = [];

  lastInvite: InviteNotification | null = null;

  private subs: Subscription[] = [];

  constructor(
    private authService: Auth,
    private wsService: Websocket,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    // 1) get current user first
    this.currentUser = this.authService.getSession();
    if (!this.currentUser) {
      this.router.navigateByUrl('/login');
      return;
    }

    // 2) connect WebSocket
    this.wsService.connect(this.currentUser);

    // 3) subscribe to online user updates (from WebSocket)
    this.subs.push(
      this.wsService.onlineUsers$.subscribe(users => {
        console.log('Lobby sees users', users);
        this.onlineUsers = users.filter(u => u.userId !== this.currentUser!.userId);
      })
    );

    // 5) invitations
    this.subs.push(
      this.wsService.invites$.subscribe(invite => {
        this.lastInvite = invite;
        if (invite.type === 'ACCEPT' && invite.gameId != null) {
          this.router.navigate(['/game', invite.gameId]);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  invite(user: OnlineUser): void {
    this.wsService.sendInvite(user.userId);
  }

  acceptInvite(): void {
    if (!this.lastInvite) { return; }
    this.wsService.respondToInvite(this.lastInvite.fromUserId, true);
  }

  refuseInvite(): void {
    if (!this.lastInvite) { return; }
    this.wsService.respondToInvite(this.lastInvite.fromUserId, false);
    this.lastInvite = null;
  }

  logout(): void {
    this.authService.clearSession();
    this.wsService.disconnect();
    this.router.navigateByUrl('/login');
  }
}
