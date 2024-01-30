import {Injectable} from '@angular/core';
import {ActivatedRoute, ActivatedRouteSnapshot, Router, RouterStateSnapshot} from '@angular/router';
import {UserService} from '../services/user.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard  {

  constructor(private userService: UserService,
              private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    let wantedRoute = state.url;

    if (this.userService.isLoggedIn()) {
      if (wantedRoute == '/') {
        this.router.navigate(['/dashboard']);
        return false;
      }

      return true;
    } else {
      if (wantedRoute == '/') {
        return true;
      }

      this.router.navigate(['/login']);
      return false;
    }
  }
}
