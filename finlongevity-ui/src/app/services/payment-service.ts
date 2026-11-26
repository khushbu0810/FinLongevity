import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PaymentService {
    private readonly base = `${environment.apiUrl}/api/payments`;
    constructor(private readonly http: HttpClient) { }
    getMyPayments<T>(): Observable<T> { return this.http.get<T>(this.base); }
    createPayment<T>(body: unknown): Observable<T> { return this.http.post<T>(this.base, body); }
    getPayment<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/${id}`); }
    processMockPayment<T>(id: string, outcome: 'SUCCESS' | 'FAILED' = 'SUCCESS'): Observable<T> {
        return this.http.post<T>(`${this.base}/${id}/process-mock`, {}, {
            params: new HttpParams().set('outcome', outcome)
        });
    }
    getPaymentEvents<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/${id}/events`); }
}
