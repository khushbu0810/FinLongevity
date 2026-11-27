import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class GoalsService {
    private readonly base = `${environment.apiUrl}/api/goals`;
    constructor(private readonly http: HttpClient) { }
    getMyGoals<T>(): Observable<T> { return this.http.get<T>(this.base); }
    createGoal<T>(body: unknown): Observable<T> { return this.http.post<T>(this.base, body); }
    getGoal<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/${id}`); }
    updateGoal<T>(id: string, body: unknown): Observable<T> { return this.http.put<T>(`${this.base}/${id}`, body); }
    changeStatus<T>(id: string, status: 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED'): Observable<T> {
        return this.http.patch<T>(`${this.base}/${id}/status`, {}, { params: new HttpParams().set('status', status) });
    }
    getMembers<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/${id}/members`); }
    addMember<T>(id: string, body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/${id}/members`, body); }
    getContributions<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/${id}/contributions`); }
    createContribution<T>(id: string, body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/${id}/contributions`, body); }
    getAnalytics<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/${id}/analytics`); }
}
