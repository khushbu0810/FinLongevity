import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class InsuranceService {
    private readonly base = `${environment.apiUrl}/api/insurance`;
    constructor(private readonly http: HttpClient) { }
    getMyApplications<T>(): Observable<T> { return this.http.get<T>(`${this.base}/applications`); }
    createApplication<T>(body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/applications`, body); }
    getApplication<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/applications/${id}`); }
    submitApplication<T>(id: string): Observable<T> { return this.http.patch<T>(`${this.base}/applications/${id}/submit`, {}); }
    getMyPolicies<T>(): Observable<T> { return this.http.get<T>(`${this.base}/policies`); }
    getPolicy<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/policies/${id}`); }
}
