import {Component, OnInit} from '@angular/core';
import {
  AbstractControl,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators
} from "@angular/forms";
import {UserService} from '../../services/user.service';
import {Router} from "@angular/router";
import {RegisterDto} from "../../dtos/registerDto";
import {PasswordEncoder} from "../../utils/passwordEncoder";
import {ToastrService} from "ngx-toastr";
import {ErrorHandler} from "../../services/errorHandler";


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {

  registerForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  // Error flag
  error = false;
  errorPasswordsNotSame: boolean = false;

  isInputFocused: { [key: string]: boolean } = {};

  showPasswords: boolean = false;
  showPassword1: boolean = false;

  constructor(private formBuilder: UntypedFormBuilder,
              private authService: UserService,
              private passwordEncoder: PasswordEncoder,
              private errorHandler: ErrorHandler,
              private router: Router,
              private notification: ToastrService) {
    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required, this.trimmedMinLength(3), Validators.maxLength(255)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      password2: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  trimmedMinLength(minLength: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value || '';
      const trimmedValue = value.trim();
      return trimmedValue.length < minLength ? {
        'trimmedMinLength': {
          requiredLength: minLength,
          actualLength: trimmedValue.length
        }
      } : null;
    };
  }

  /**
   * Form validation will start after the method is called, additionally an LoginDto will be sent
   */
  registerUser() {
    this.submitted = true;
    if (this.registerForm.valid && this.registerForm.controls.password.value == this.registerForm.controls.password2.value) {
      const name: string = <string>this.registerForm.controls.username.value;
      const email: string = <string>this.registerForm.controls.email.value;
      const password: string = <string>this.registerForm.controls.password.value;
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
    console.log('Try to register user: ' + authRequest.name + ' with email: ' + authRequest.email + ' with encoded password ' + authRequest.passwordEncoded);
    this.authService.registerUser(authRequest).subscribe({
      next: (data) => {
        console.log('Successfully registered user: ' + authRequest.email);
        this.notification.success('Redirecting to Login', 'You have successfully registered!');
        // Redirect to login page after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: error => {

        let errorObj = this.errorHandler.getErrorObject(error);

        switch (errorObj.status) {
          case 409:
            console.warn("Register failed: ", error);
            this.notification.warning("An account already exists for this Email or Username");
            break;
          default:
            this.errorHandler.handleApiError(errorObj);
            break;
        }

      }
    });
  }

  ngOnInit() {
  }

  togglePasswordVisibility() {
    this.showPasswords = !this.showPasswords;
  }

  //TODO: refactor this
  togglePasswordVisibility1() {
    this.showPassword1 = !this.showPassword1;
  }

  /**
   * Update the input focus flag in order to show/hide the label on the input field
   */
  updateInputFocus(attribute: string) {
    this.isInputFocused[attribute] = this.registerForm.get(attribute).value !== '';
  }

}
