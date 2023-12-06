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

    const authBaseUri = this.globals.backendUri + '/authentication';
    const logoutUri = authBaseUri + '/logout';

    // Do not intercept authentication (login) requests
    if (req.url.startsWith(authBaseUri) && req.url != logoutUri) {
      return next.handle(req);
    }

    const authReq = req.clone({
      headers: req.headers.set('Authorization', this.authService.getToken())
    });

    return next.handle(authReq);
  }
}
