import {Injectable} from '@angular/core';
import {LoginDto} from '../dtos/loginDto';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {jwtDecode} from 'jwt-decode';
import {Globals} from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private authBaseUri: string = this.globals.backendUri + '/authentication';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Login in the user. If it was successful, a valid JWT token will be stored
   *
   * @param authRequest User data
   */
  loginUser(authRequest: LoginDto): Observable<string> {
    console.debug("Logging in as '" + authRequest.email + "'");
    return this.httpClient.post(this.authBaseUri + "/login", authRequest, {responseType: 'text'})
      .pipe(
        tap((authResponse: string) => this.setToken(authResponse))
      );
  }

  logoutUser() {
    console.debug("Logging out");

    this.httpClient.post(this.authBaseUri + "/logout", this.getToken())
      .subscribe({
        next: success => {
          if (success) {
            console.log("Logged out from backend");
          } else {
            console.warn("Backend denied logout, rely on client-side logout only");
          }
        },
        error: error => {
          console.warn("Could not log out from backend, rely on client-side logout only", error);
        }
      })
    localStorage.removeItem('authToken');
  }

  /**
   * Check if a valid JWT token is saved in the localStorage
   */
  isLoggedIn() {
    return !!this.getToken() && (this.getTokenExpirationDate(this.getToken()).valueOf() > new Date().valueOf());
  }

  getToken() {
    return localStorage.getItem('authToken');
  }

  private setToken(authResponse: string) {
    localStorage.setItem('authToken', authResponse);
  }

  private getTokenExpirationDate(token: string): Date {

    const decoded: any = jwtDecode(token);
    if (decoded.exp === undefined) {
      return null;
    }

    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  }

}
