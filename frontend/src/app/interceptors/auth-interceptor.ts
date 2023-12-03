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
    const authUri = this.globals.backendUri + '/authentication/login';

    // Do not intercept authentication (login) requests
    if (req.url === authUri) {
      return next.handle(req);
    }

    const authReq = req.clone({
      headers: req.headers.set('Authorization', this.authService.getToken())
    });

    return next.handle(authReq);
  }
}
