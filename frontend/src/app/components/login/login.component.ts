import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {UserService} from '../../services/user.service';
import {LoginDto} from '../../dtos/loginDto';
import {PasswordEncoder} from "../../utils/passwordEncoder";


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;

  // Authentication Error
  authenticationError: boolean = false;
  authenticationErrorMessage: string | null;

  constructor(private formBuilder: UntypedFormBuilder, private authService: UserService, private passwordEncoder: PasswordEncoder, private router: Router) {
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
        this.router.navigate(['/message']);
      },
      error: error => {
        console.warn('Could not log in', error)
        this.authenticationError = true;
        if (typeof error.error === 'object') {
          this.authenticationErrorMessage = error.error.error;
        } else {
          this.authenticationErrorMessage = error.error;
        }
      }
    });
  }

  /**
   * Error flag will be deactivated, which clears the error message
   */
  vanishAuthenticationError() {
    this.authenticationError = false;
    this.authenticationErrorMessage = null;
  }

  ngOnInit() {
  }

}
