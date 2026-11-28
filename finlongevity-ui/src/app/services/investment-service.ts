import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { InvestmentValuationRequest } from '../models/investment-valuation.models';

@Injectable({ providedIn: 'root' })
export class InvestmentService {
    private readonly base = `${environment.apiUrl}/api/investments`;
    constructor(private readonly http: HttpClient) { }
    getMyInvestments<T>(): Observable<T> { return this.http.get<T>(this.base); }
    createInvestment<T>(body: unknown): Observable<T> { return this.http.post<T>(this.base, body); }
    getInvestment<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/${id}`); }
    cancelDraft<T>(id: string): Observable<T> { return this.http.patch<T>(`${this.base}/${id}/cancel`, {}); }
    getTransactions<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/${id}/transactions`); }
    addTransaction<T>(id: string, body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/${id}/transactions`, body); }
    getValuationOverview<T>(): Observable<T> { return this.http.get<T>(`${this.base}/valuation-overview`); }
    getValuationHistory<T>(investmentId: string): Observable<T> { return this.http.get<T>(`${this.base}/${investmentId}/valuations`); }
    recordValuation<T>(investmentId: string, body: InvestmentValuationRequest): Observable<T> {
        return this.http.post<T>(`${this.base}/${investmentId}/valuations`, body);
    }
}
