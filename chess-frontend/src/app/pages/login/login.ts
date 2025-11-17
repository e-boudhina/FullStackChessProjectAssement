import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { Auth } from '../../services/auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [
    FormsModule]
    ,
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  registerUsername = '';
  registerPassword = '';

  loginUsername = '';
  loginPassword = '';

  errorMessage = '';

  constructor(
    private authService: Auth,
    private router: Router
  ) {}

  onRegister(): void {
    this.errorMessage = '';
    this.authService.register({
      username: this.registerUsername,
      password: this.registerPassword
    }).subscribe({
      next: (res) => {
        this.authService.saveSession(res);
        this.router.navigate(['/lobby']);
      },
      error: (err) => {
        this.errorMessage = 'Registration failed';
        console.error(err);
      }
    });
  }

  onLogin(): void {
    this.errorMessage = '';
    this.authService.login({
      username: this.loginUsername,
      password: this.loginPassword
    }).subscribe({
      next: (res) => {
        this.authService.saveSession(res);
        this.router.navigate(['/lobby']);
      },
      error: (err) => {
        this.errorMessage = 'Login failed';
        console.error(err);
      }
    });
  }
}
