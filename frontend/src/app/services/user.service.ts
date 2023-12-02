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
    console.debug("url: ", this.authBaseUri + "/login");
    return this.httpClient.post(this.authBaseUri + "/login", authRequest, {responseType: 'text'})
      .pipe(
        tap((authResponse: string) => this.setToken(authResponse))
      );
  }


  /**
   * Check if a valid JWT token is saved in the localStorage
   */
  isLoggedIn() {
    return !!this.getToken() && (this.getTokenExpirationDate(this.getToken()).valueOf() > new Date().valueOf());
  }

  logoutUser() {
    console.log('Logout');
    localStorage.removeItem('authToken');
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
