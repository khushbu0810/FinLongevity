import { computed, Injectable, signal } from '@angular/core';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from '../models/auth.models';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

const TOKEN_KEY = 'finlongevity_access_token';
const USER_KEY = 'finlongevity_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly baseUrl = `${environment.apiUrl}/api/identity`;
    private readonly currentUserState = signal<UserResponse | null>(this.readUser());
    readonly currentUser = this.currentUserState.asReadonly();
    readonly isAuthenticated = computed(
        () => !!this.currentUserState() && !!this.getToken()
    );
    constructor(private readonly http: HttpClient) { }

    register(request: RegisterRequest): Observable<UserResponse> {
        return this.http.post<UserResponse>(`${this.baseUrl}/auth/register`, request);
    }

    login(request: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, request).pipe(
            tap(response => {
                sessionStorage.setItem(TOKEN_KEY, response.accessToken);
                sessionStorage.setItem(USER_KEY, JSON.stringify(response.user));
                this.currentUserState.set(response.user);
            })
        );
    }

    loadCurrentUser(): Observable<UserResponse> {
        return this.http.get<UserResponse>(`${this.baseUrl}/users/me`).pipe(
            tap(user => {
                sessionStorage.setItem(USER_KEY, JSON.stringify(user));
                this.currentUserState.set(user);
            })
        );
    }

    getToken(): string | null {
        return typeof sessionStorage === 'undefined' ? null : sessionStorage.getItem(TOKEN_KEY);
    }

    logout(): void {
        sessionStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem(USER_KEY);
        this.currentUserState.set(null);
    }

    private readUser(): UserResponse | null {
        if (typeof sessionStorage === 'undefined') return null;
        const raw = sessionStorage.getItem(USER_KEY);
        if (!raw) return null;
        try { return JSON.parse(raw) as UserResponse; } catch { return null; }
    }
}
