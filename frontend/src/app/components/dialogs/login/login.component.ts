import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {UserService} from '../../../services/user.service';
import {LoginDto} from '../../../dtos/loginDto';
import {PasswordEncoder} from "../../../utils/passwordEncoder";
import {ToastrService} from "ngx-toastr";
import {ApiErrorHandler} from "../../../services/apiErrorHandler";
import {ErrorDto} from "../../../dtos/errorDto";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;

  showPasswords: boolean = false;

  errorEmailNotFound: boolean = false;
  errorPasswordWrong: boolean = false;

  isInputFocused: {[key: string]: boolean } = {};

  loginError: boolean = false;

  constructor(private formBuilder: UntypedFormBuilder,
              private authService: UserService,
              private apiErrorHandler: ApiErrorHandler,
              private passwordEncoder: PasswordEncoder,
              private router: Router,
              private notification: ToastrService) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  /**
   * Form validation will start after the method is called, additionally an LoginDto will be sent
   */
  onSubmitLogin() {
    this.submitted = true;
    if (this.loginForm.valid) {
      const email: string = <string> this.loginForm.controls.email.value;
      const password: string = <string> this.loginForm.controls.password.value;
      const encodedPassword = this.passwordEncoder.encodePassword(password);

      const loginDto: LoginDto = new LoginDto(email, encodedPassword);

      this.authenticateUser(loginDto);
    } else {
      console.log('Invalid login form input');
    }
  }

  /**
   * Send authentication data to the authService. If the authentication was successfully, the user will be forwarded to the message page
   *
   * @param authRequest authentication data from the user login form
   */
  authenticateUser(authRequest: LoginDto) {
    this.authService.loginUser(authRequest).subscribe({
      next: (data) => {
        console.log('Logged in as ' + authRequest.email);
        this.router.navigate(['/dashboard']);
      },
      error: error => {

        let errorJson = JSON.parse(error.error);

        if (errorJson == undefined || errorJson["statusCode"] == undefined) {
          this.apiErrorHandler.handleApiError(error);
        } else {

          switch (errorJson["statusCode"]) {
            case 401: // unauthorized -> invalid credentials
              console.warn("Invalid Credentials");
              this.loginError = true;
              this.notification.warning("Wrong password or user doesn't exist");
              break;
            default:
              this.apiErrorHandler.handleApiError(error);
              break;
          }

        }

      }
    });
  }

  ngOnInit() {
  }

  togglePasswordVisibility() {
    this.showPasswords = !this.showPasswords;
  }

  /**
   * Update the input focus flag in order to show/hide the label on the input field
   */
  updateInputFocus(attribute: string) {
    this.isInputFocused[attribute] = this.loginForm.get(attribute).value !== '';
  }

}

