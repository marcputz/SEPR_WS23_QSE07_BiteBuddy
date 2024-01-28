import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {UserService} from '../services/user.service';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: UserService, private globals: Globals) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    const uriExceptions: string[] = [
      this.globals.backendUri + '/user/login',
      this.globals.backendUri + '/user/register',
      this.globals.backendUri + '/user/request_password_reset',
      this.globals.backendUri + '/user/password_reset',
    ];


    // Do not intercept authentication requests
    if (uriExceptions.includes(req.url)) {
      return next.handle(req);
    }

    const authReq = req.clone({
      headers: req.headers.set('Authorization', this.authService.getToken())
    });

    return next.handle(authReq);
  }
}
