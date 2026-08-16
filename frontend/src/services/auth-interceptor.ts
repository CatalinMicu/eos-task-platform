import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { LocalStorageUtils } from '../utils/local-storage-utils';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('/login') || req.url.includes('/register')) {
    return next(req);
  }

  const token = LocalStorageUtils.getItem(LocalStorageUtils.tokenKey);

  if (!token) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }),
  ).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        LocalStorageUtils.deleteItem(LocalStorageUtils.tokenKey);
        window.location.href = '/login';
      }

      return throwError(() => error);
    }),
  );
};
