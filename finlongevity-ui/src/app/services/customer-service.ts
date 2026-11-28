import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CustomerService {
    private readonly base = `${environment.apiUrl}/api/customers/me`;
    constructor(private readonly http: HttpClient) { }

    getProfile<T>(): Observable<T> { return this.http.get<T>(this.base); }
    createProfile<T>(body: unknown): Observable<T> { return this.http.post<T>(this.base, body); }
    updateProfile<T>(body: unknown): Observable<T> { return this.http.put<T>(this.base, body); }

    getEmployment<T>(): Observable<T> { return this.http.get<T>(`${this.base}/employment`); }
    saveEmployment<T>(body: unknown): Observable<T> { return this.http.put<T>(`${this.base}/employment`, body); }

    getIncomeSources<T>(): Observable<T> { return this.http.get<T>(`${this.base}/income-sources`); }
    addIncome<T>(body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/income-sources`, body); }
    updateIncome<T>(id: string, body: unknown): Observable<T> { return this.http.put<T>(`${this.base}/income-sources/${id}`, body); }
    deleteIncome(id: string): Observable<void> { return this.http.delete<void>(`${this.base}/income-sources/${id}`); }

    getExpenses<T>(): Observable<T> { return this.http.get<T>(`${this.base}/expenses`); }
    getActiveExpenses<T>(): Observable<T> { return this.http.get<T>(`${this.base}/expenses/active`); }
    getActiveExpensesByCurrency<T>(currency: string): Observable<T> {
        return this.http.get<T>(`${this.base}/expenses/active/currency/${encodeURIComponent(currency.toUpperCase())}`);
    }
    createExpense<T>(body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/expenses`, body); }
    updateExpense<T>(id: string, body: unknown): Observable<T> { return this.http.put<T>(`${this.base}/expenses/${id}`, body); }
    deleteExpense(id: string): Observable<void> { return this.http.delete<void>(`${this.base}/expenses/${id}`); }

    getAssets<T>(): Observable<T> { return this.http.get<T>(`${this.base}/assets`); }
    createAsset<T>(body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/assets`, body); }
    updateAsset<T>(id: string, body: unknown): Observable<T> { return this.http.put<T>(`${this.base}/assets/${id}`, body); }
    deleteAsset(id: string): Observable<void> { return this.http.delete<void>(`${this.base}/assets/${id}`); }

    getLiabilities<T>(): Observable<T> { return this.http.get<T>(`${this.base}/liabilities`); }
    createLiability<T>(body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/liabilities`, body); }
    updateLiability<T>(id: string, body: unknown): Observable<T> { return this.http.put<T>(`${this.base}/liabilities/${id}`, body); }
    deleteLiability(id: string): Observable<void> { return this.http.delete<void>(`${this.base}/liabilities/${id}`); }

    getNetWorth(currency = 'INR'): Observable<unknown> {
        return this.http.get(`${this.base}/net-worth`, { params: new HttpParams().set('currency', currency) });
    }

    getRiskProfile<T>(): Observable<T> { return this.http.get<T>(`${this.base}/risk-profile`); }
    saveRiskProfile<T>(body: unknown): Observable<T> { return this.http.put<T>(`${this.base}/risk-profile`, body); }

    getNominees<T>(): Observable<T> { return this.http.get<T>(`${this.base}/nominees`); }
    createNominee<T>(body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/nominees`, body); }
    updateNominee<T>(id: string, body: unknown): Observable<T> { return this.http.put<T>(`${this.base}/nominees/${id}`, body); }
    deleteNominee(id: string): Observable<void> { return this.http.delete<void>(`${this.base}/nominees/${id}`); }

    getHousehold<T>(): Observable<T> { return this.http.get<T>(`${this.base}/household`); }
    saveHousehold<T>(body: unknown): Observable<T> { return this.http.put<T>(`${this.base}/household`, body); }
    createHousehold<T>(body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/household`, body); }
    getHouseholdMembers<T>(): Observable<T> { return this.http.get<T>(`${this.base}/household/members`); }
    addHouseholdMember<T>(body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/household/members`, body); }
    updateHouseholdMember<T>(id: string, body: unknown): Observable<T> { return this.http.put<T>(`${this.base}/household/members/${id}`, body); }
    deleteHouseholdMember(id: string): Observable<void> { return this.http.delete<void>(`${this.base}/household/members/${id}`); }
}
