import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {AuthService} from '../services/auth.service';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private globals: Globals) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    const uriExceptions: string[] = [
      this.globals.backendUri + '/authentication/login',
      this.globals.backendUri + '/authentication/register',
      this.globals.backendUri + '/authentication/request_password_reset',
      this.globals.backendUri + '/authentication/password_reset',
    ];


    // Do not intercept authentication (login) requests
    if (uriExceptions.includes(req.url)) {
      return next.handle(req);
    }

    const authReq = req.clone({
      headers: req.headers.set('Authorization', this.authService.getToken())
    });

    return next.handle(authReq);
  }
}
