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
  // Error flag
  error = false;
  errorMessage = '';

  constructor(private formBuilder: UntypedFormBuilder, private authService: UserService, private passwordEncoder: PasswordEncoder, private router: Router) {
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  /**
   * Form validation will start after the method is called, additionally an LoginDto will be sent
   */
  loginUser() {
    this.submitted = true;
    if (this.loginForm.valid) {
      const username: string = <string> this.loginForm.controls.username.value;
      const password: string = <string> this.loginForm.controls.password.value;
      const encodedPassword = this.passwordEncoder.encodePassword(password);
      const loginDto: LoginDto = new LoginDto(username, encodedPassword);
      this.authenticateUser(loginDto);
    } else {
      console.log('Invalid input');
    }
  }

  /**
   * Send authentication data to the authService. If the authentication was successfully, the user will be forwarded to the message page
   *
   * @param authRequest authentication data from the user login form
   */
  authenticateUser(authRequest: LoginDto) {
    console.log('Try to authenticate user: ' + authRequest.email + ' with encoded password ' + authRequest.password);
    this.authService.loginUser(authRequest).subscribe({
      next: (data) => {
        console.log('Successfully logged in user: ' + authRequest.email, data);
        this.router.navigate(['/message']);
      },
      error: error => {
        console.log('Could not log in due to:');
        console.log(error);
        this.error = true;
        if (typeof error.error === 'object') {
          this.errorMessage = error.error.error;
        } else {
          this.errorMessage = error.error;
        }
      }
    });
  }

  /**
   * Error flag will be deactivated, which clears the error message
   */
  vanishError() {
    this.error = false;
  }

  ngOnInit() {
  }

}
