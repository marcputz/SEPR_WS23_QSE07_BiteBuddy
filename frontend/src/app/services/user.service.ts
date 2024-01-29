import {EventEmitter, Injectable} from '@angular/core';
import {LoginDto} from '../dtos/loginDto';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {jwtDecode} from 'jwt-decode';
import {Globals} from '../global/globals';
import {RegisterDto} from "../dtos/registerDto";
import {UserSettingsDto} from '../dtos/userSettingsDto';
import {UpdateAuthenticationSettingsDto} from '../dtos/updateAuthenticationSettingsDto';
import {ResetPasswordDto} from "../dtos/resetPasswordDto";
import {UpdateUserSettingsDto} from '../dtos/updateUserSettingsDto';
import {ErrorHandler} from "./errorHandler";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private authBaseUri: string = this.globals.backendUri + '/user';

  updateEvent: EventEmitter<void> = new EventEmitter<void>();

  constructor(private httpClient: HttpClient, private globals: Globals, private apiErrorHandler: ErrorHandler) {
  }

  triggerUpdate() {
    this.updateEvent.emit();
  }

  /**
   * Login in the user. If it was successful, a valid JWT token will be stored
   *
   * @param authRequest User data
   */
  loginUser(authRequest: LoginDto): Observable<string> {
    console.trace("Logging in as '" + authRequest.email + "'");

    return this.httpClient.post(this.authBaseUri + "/login", authRequest, {responseType: 'text'})
      .pipe(
        tap((authResponse: string) => this.setToken(authResponse))
      );
  }

  registerUser(authRequest: RegisterDto): Observable<string> {
    console.trace("Registering as '" + authRequest.email + "'");

    return this.httpClient.post(this.authBaseUri + "/register", authRequest, {responseType: 'text'})
      .pipe(
        tap((authResponse: string) => this.setToken(authResponse))
      );
  }

  getUser(): Observable<UserSettingsDto> {
    return this.httpClient.get<UserSettingsDto>(`${this.authBaseUri}/settings`);
  }

  updateUserAuthentication(updateAuthenticationSettingsDto: UpdateAuthenticationSettingsDto): Observable<UserSettingsDto> {
    return this.httpClient.put<UserSettingsDto>(
      this.authBaseUri + "/settings/authentication",
      updateAuthenticationSettingsDto,
    );
  }

  updateUserSettings(updateUserSettingsDto: UpdateUserSettingsDto): Observable<UserSettingsDto> {
    return this.httpClient.put<UserSettingsDto>(
      this.authBaseUri + "/settings",
      updateUserSettingsDto,
    );
  }

  logoutUser() {
    console.log("Logging out");

    this.httpClient.post(this.authBaseUri + "/logout", this.getToken())
      .subscribe({
        next: success => {
          console.log("Logged out from backend");
        },
        error: error => {
          this.apiErrorHandler.handleApiError(error);
        }
      })

    localStorage.removeItem('authToken');
  }

  requestPasswordReset(userEmail: string): Observable<any> {
    console.log("Requesting password reset for '" + userEmail + "' from server");

    return this.httpClient.post(this.authBaseUri + "/request_password_reset", "{\"email\":\"" + userEmail + "\"}", {responseType: 'text'})
  }

  resetPassword(dto: ResetPasswordDto): Observable<any> {
    return this.httpClient.post(this.authBaseUri + "/password_reset", dto, {responseType: 'text'});
  }

  /**
   * Checks if the user is really logged in via backend response.
   * Not using isLoggedIn, since this is used elsewhere and checks if the frontend user is valid.
   */
  isLoggedInForCreationComponent() {
    return this.httpClient.get(this.authBaseUri + "/request_login_status");
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

  public getTokenUsername(token: string): string | null {
    const decoded: any = jwtDecode(token);
    return decoded.username ? decoded.username : null;
  }

  public getTokenEmail(token: string): string | null {
    const decoded: any = jwtDecode(token);
    return decoded.email ? decoded.email : null;
  }

}
