import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { User } from '../../shared/models/user.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/v1/auth';
  private userSubject = new BehaviorSubject<User | null>(this.getUserFromToken());
  public user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: { username: string; password: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem('accessToken', response.accessToken);
        this.userSubject.next(this.getUserFromToken());
        this.router.navigate(['/']);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    this.userSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    const user = this.getUserFromToken();
    return user ? user.exp * 1000 > Date.now() : false;
  }

  private getUserFromToken(): User | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      return jwtDecode<User>(token);
    } catch (error) {
      return null;
    }
  }
}
