import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Lobby } from './pages/lobby/lobby';
import { Game } from './pages/game/game';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'lobby', component: Lobby },
  { path: 'game/:id', component: Game },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
