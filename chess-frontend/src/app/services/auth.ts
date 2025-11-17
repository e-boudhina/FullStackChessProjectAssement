import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';


export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  userId: number;
  username: string;
  token: string;
}

@Injectable({
  providedIn: 'root',
})
export class Auth {

  private readonly baseUrl = 'http://localhost:8075/auth';

  constructor(private http: HttpClient) { }

  register(payload: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, payload);
  }

  login(payload: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, payload);
  }

  saveSession(auth: AuthResponse): void {
    localStorage.setItem('chess_user', JSON.stringify(auth));
  }

  getSession(): AuthResponse | null {
    const raw = localStorage.getItem('chess_user');
    if (!raw) { return null; }
    try {
      return JSON.parse(raw) as AuthResponse;
    } catch {
      return null;
    }
  }

  clearSession(): void {
    localStorage.removeItem('chess_user');
  }
}
