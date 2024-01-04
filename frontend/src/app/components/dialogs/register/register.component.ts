import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../../services/auth.service";
import {Router} from "@angular/router";
import {RegisterDto} from "../../../dtos/registerDto";
import {PasswordEncoder} from "../../../utils/passwordEncoder";
import {ToastrService} from "ngx-toastr";


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit{

  registerForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  // Error flag
  error = false;
  errorMessage = '';
  errorMailAlreadyExists: boolean = false;
  errorPasswordsNotSame: boolean = false;

  constructor(private formBuilder: UntypedFormBuilder,
              private authService: AuthService,
              private passwordEncoder: PasswordEncoder,
              private router: Router,
              private notification: ToastrService) {
    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
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
      this.errorPasswordsNotSame = true;
      this.registerForm.controls['password2'].setErrors({passwordsDifferent: true});
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
        this.router.navigate(['/profile']);
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
        let errorObject;
        if (typeof error.error === 'object') {
          errorObject = error.error;
        } else {
          errorObject = error;
        }

        let message: string = errorObject.error;
        let status = errorObject.status;

        switch (status) {
          case 400:
            if (message.indexOf("Invalid email format") >= 0) {
              console.log('cas1:');
              this.registerForm.controls['email'].setErrors({emailNotValid: true});
              break;
            }
            if (message.indexOf("Email already exists") >= 0) {
              console.log('cas1:');
              this.registerForm.controls['email'].setErrors({emailAlreadyExists: true});
              break;
            }
            if (message.indexOf("User with this Name already exists") >= 0) {
              console.log('cas1:');
              this.registerForm.controls['username'].setErrors({userAlreadyExists: true});
              break;
            }
          default: this.notification.error("Error while logging in, try again later."); break;
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
