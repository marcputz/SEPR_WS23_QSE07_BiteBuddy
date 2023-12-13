import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../../../services/auth.service';
import {LoginDto} from '../../../dtos/loginDto';
import {PasswordEncoder} from "../../../utils/passwordEncoder";
import {ToastrService} from "ngx-toastr";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;

  errorEmailNotFound: boolean = false;
  errorPasswordWrong: boolean = false;

  constructor(private formBuilder: UntypedFormBuilder,
              private authService: AuthService,
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
        this.router.navigate(['/message']);
      },
      error: error => {
        console.error('Could not log in', error);

        let errorObject;
        if (typeof error.error === 'object') {
          errorObject = error.error;
        } else {
          errorObject = error;
        }

        let message: string = errorObject.error;
        let status = errorObject.status;

        switch (status) {
          case 401:
            if (message.indexOf("does not exist") >= 0) {
              // user does not exist
              console.warn("User does not exist");
              this.loginForm.controls['email'].setErrors({userNotFound: true});
              break;
            }
            if (message.indexOf("Wrong Password") >= 0) {
              // password wrong error
              console.warn("Wrong password");
              this.loginForm.controls['password'].setErrors({wrongPassword: true});
              break;
            }
            // other type of authentication error
            this.notification.error(message); break;

          default: this.notification.error("Error while logging in, try again later."); break;
        }

      }
    });
  }

  ngOnInit() {
  }

}

