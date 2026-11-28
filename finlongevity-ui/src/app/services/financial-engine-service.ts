import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FinancialEngineService {
    private readonly base = `${environment.apiUrl}/api/financial-engine`;
    constructor(private readonly http: HttpClient) { }
    getRetirementPlanningPrefill<T>(currency = 'INR', maximumValuationAgeDays = 90, plannedRetirementAge?: number): Observable<T> {
        let params = new HttpParams()
            .set('currency', currency)
            .set('maximumValuationAgeDays', maximumValuationAgeDays);
        if (plannedRetirementAge != null) params = params.set('plannedRetirementAge', plannedRetirementAge);
        return this.http.get<T>(`${this.base}/retirement/prefill/me`, { params });
    }
    getDebtRetirementAssessment<T>(currency = 'INR', plannedRetirementAge?: number): Observable<T> {
        let params = new HttpParams().set('currency', currency);
        if (plannedRetirementAge != null) params = params.set('plannedRetirementAge', plannedRetirementAge);
        return this.http.get<T>(`${this.base}/debt/retirement-assessment/me`, { params });
    }
    analyzeMyFinancials<T>(currency = 'INR'): Observable<T> { return this.http.post<T>(`${this.base}/analyze/me`, {}, { params: new HttpParams().set('currency', currency) }); }
    analyzeSnapshot<T>(body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/analyze`, body); }
    getDashboard<T>(): Observable<T> { return this.http.get<T>(`${this.base}/dashboard`); }
    getMyAnalyses<T>(): Observable<T> { return this.http.get<T>(`${this.base}/analyses`); }
    getAnalysis<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/analyses/${id}`); }
    getProjections<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/analyses/${id}/projections`); }
    createRetirementProjection<T>(id: string, body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/analyses/${id}/retirement-projection`, body); }
}
