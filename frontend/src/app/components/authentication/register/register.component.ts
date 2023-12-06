import {Component} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../../services/auth.service";
import {Router} from "@angular/router";
import {RegisterDto} from "../../../dtos/registerDto";
import {PasswordEncoder} from "../../../utils/passwordEncoder";


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {

  registerForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  // Error flag
  error = false;
  errorMessage = '';

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private passwordEncoder: PasswordEncoder, private router: Router) {
    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      email: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      password2: ['', [Validators.required, Validators.minLength(8)]]
    });
  }


  /**
   * Form validation will start after the method is called, additionally an LoginDto will be sent
   */
  registerUser() {
    this.submitted = true;
    if (this.registerForm.valid && this.registerForm.controls.password.value == this.registerForm.controls.password2.value) {
      const name: string = <string> this.registerForm.controls.username.value;
      const email: string = <string> this.registerForm.controls.email.value;
      const password: string = <string> this.registerForm.controls.password.value;
      const encodedPassword = this.passwordEncoder.encodePassword(password);

      const registerDto: RegisterDto = new RegisterDto(email, name, encodedPassword);

      this.authenticateUser(registerDto);
    } else if (this.registerForm.controls.password.value != this.registerForm.controls.password2.value) {
      console.log('Passwords not the same');
    } else {
      console.log('Invalid input');
    }
  }

  /**
   * Send authentication data to the authService. If the authentication was successfully, the user will be forwarded to the message page
   *
   * @param authRequest authentication data from the user login form
   */
  authenticateUser(authRequest: RegisterDto) {
    console.log('Try to register user: ' + authRequest.name + ' with email: ' + authRequest.email + ' with encoded password ' + authRequest.passwordEncoded );
    this.authService.registerUser(authRequest).subscribe({
      next: (data) => {
        console.log('Successfully registered user: ' + authRequest.email);
        this.router.navigate(['']);
      },
      error: error => {
        console.log('Could not register user due to:');
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
