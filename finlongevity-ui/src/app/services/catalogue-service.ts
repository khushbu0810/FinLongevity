import { Service } from '@angular/core';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CatalogueService {
    private readonly base = environment.apiUrl;
    constructor(private readonly http: HttpClient) { }
    getProviders<T>(status?: string): Observable<T> {
        const params = status ? new HttpParams().set('status', status) : undefined;
        return this.http.get<T>(`${this.base}/api/providers`, { params });
    }
    getProvider<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/api/providers/${id}`); }
    createProvider<T>(body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/api/providers`, body); }
    updateProviderStatus<T>(id: string, status: string): Observable<T> {
        return this.http.patch<T>(`${this.base}/api/providers/${id}/status`, {}, { params: new HttpParams().set('status', status) });
    }
    getProviderProducts<T>(providerId: string): Observable<T> { return this.http.get<T>(`${this.base}/api/products/${providerId}/products`); }
    createProduct<T>(providerId: string, body: unknown): Observable<T> { return this.http.post<T>(`${this.base}/api/products/${providerId}/products`, body); }
    getProduct<T>(id: string): Observable<T> { return this.http.get<T>(`${this.base}/api/products/${id}`); }
    updateProduct<T>(id: string, body: unknown): Observable<T> { return this.http.put<T>(`${this.base}/api/products/${id}`, body); }
    updateProductStatus<T>(id: string, status: string): Observable<T> {
        return this.http.patch<T>(`${this.base}/api/products/${id}/status`, {}, { params: new HttpParams().set('status', status) });
    }
    getCatalogue<T>(type?: string): Observable<T> {
        const params = type ? new HttpParams().set('type', type) : undefined;
        return this.http.get<T>(`${this.base}/api/products/catalogue`, { params });
    }
}
