import {Injectable} from '@angular/core';
import {LoginDto} from '../dtos/loginDto';
import {Observable} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {jwtDecode} from 'jwt-decode';
import {Globals} from '../global/globals';
import {RegisterComponent} from "../components/authentication/register/register.component";
import {RegisterDto} from "../dtos/registerDto";
import {UserSettingsDto} from '../dtos/userSettingsDto';
import {UpdateUserSettingsDto} from '../dtos/updateUserSettingsDto';
import {ResetPasswordDto} from "../dtos/resetPasswordDto";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

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

  registerUser(authRequest: RegisterDto): Observable<string> {
    console.debug("Registering as '" + authRequest.email + "'" + authRequest.name + "'" + authRequest.passwordEncoded);
    return this.httpClient.post(this.authBaseUri + "/register", authRequest, {responseType: 'text'})
      .pipe(
        tap((authResponse: string) => this.setToken(authResponse))
      );
  }

  getUser(): Observable<UserSettingsDto> {
    console.debug("Retrieving current user settings");

    const authToken = this.getToken();
    if (!authToken) {
      throw new Error('Authorization token not found');
    }

    const headers = new HttpHeaders({
      'Authorization': `${authToken}`
    });

    return this.httpClient.get<UserSettingsDto>(`${this.authBaseUri}/settings`, { headers });
  }

  updateUser(updateUserSettingsDto: UpdateUserSettingsDto): Observable<UserSettingsDto> {
    return this.httpClient.put<UserSettingsDto>(
      this.authBaseUri + "/settings",
      updateUserSettingsDto
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

  requestPasswordReset(userEmail: string): Observable<any> {
    console.debug("Requesting password reset for '" + userEmail + "' from server");
    return this.httpClient.post(this.authBaseUri + "/request_password_reset", "{\"email\":\"" + userEmail + "\"}", {responseType: 'text'})
  }

  resetPassword(dto: ResetPasswordDto): Observable<any> {
    return this.httpClient.post(this.authBaseUri + "/password_reset", dto, {responseType: 'text'});
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
