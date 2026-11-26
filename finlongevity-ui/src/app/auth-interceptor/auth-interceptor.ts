import { Component, inject } from '@angular/core';
import { AuthService } from '../services/auth-service';
import { HttpInterceptorFn } from '@angular/common/http';


export const AuthInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthService).getToken();
  if (!token || request.url.endsWith('/auth/login') || request.url.endsWith('/auth/register')) {
    return next(request);
  }
  return next(request.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  }));
};
