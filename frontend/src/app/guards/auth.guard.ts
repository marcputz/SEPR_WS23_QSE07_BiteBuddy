import {Injectable} from '@angular/core';
import { Router } from '@angular/router';
import {AuthService} from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard  {

  constructor(private userService: AuthService,
              private router: Router) {}

  canActivate(): boolean {
    if (this.userService.isLoggedIn()) {
      return true;
    } else {
      this.router.navigate(['/login']);
      return false;
    }
  }
}
