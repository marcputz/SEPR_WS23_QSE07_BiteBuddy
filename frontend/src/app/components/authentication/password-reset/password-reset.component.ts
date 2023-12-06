import {Component, OnInit} from '@angular/core';
import {
  AbstractControl, FormGroup,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators
} from "@angular/forms";
import {AuthService} from "../../../services/auth.service";
import {PasswordEncoder} from "../../../utils/passwordEncoder";
import {ActivatedRoute, Router} from "@angular/router";
import {ResetPasswordDto} from "../../../dtos/resetPasswordDto";

@Component({
  selector: 'app-password-reset',
  templateUrl: './password-reset.component.html',
  styleUrls: ['./password-reset.component.scss']
})
export class PasswordResetComponent implements OnInit {

  passwordForm: UntypedFormGroup;
  submitted: boolean = false;

  protected requestId: string | null;
  protected expirationDate: Date | null;

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private passwordEncoder: PasswordEncoder, private router: Router, private activatedRoute: ActivatedRoute) {
    this.passwordForm = this.formBuilder.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      passwordConfirm: ['', [Validators.required, Validators.minLength(8)]],
    }, {validator: matchingPasswords('password', 'passwordConfirm')});
  }

  ngOnInit(): void {
    this.requestId = this.activatedRoute.snapshot.queryParamMap.get('id');
    const expDateString = this.activatedRoute.snapshot.queryParamMap.get('exp');
    // TODO: use expiration date to show user error if link is expired
    // TODO: show user error if request ID is not given
  }

  onSubmit() {
    this.submitted = true;
    if (this.passwordForm.valid) {
      if (this.requestId != undefined) {

        const password: string = <string>this.passwordForm.controls.password.value;
        const encodedPassword = this.passwordEncoder.encodePassword(password);

        let dto = new ResetPasswordDto(this.requestId, encodedPassword);

        this.authService.resetPassword(dto).subscribe({
          next: data => {
            console.log("reset success");
          },
          error: error => {
            console.error("Could not reset password due to: ", error);
          }
        });

      } else {
        console.warn("No Request ID given in URL, cannot post request to server");
        // TODO: show error to user
      }
    }
  }

}

export function matchingPasswords(passwordKey: string, passwordConfirmationKey: string) {
  return (group: FormGroup) => {
    let passwordInput = group.controls[passwordKey];
    let passwordConfirmationInput = group.controls[passwordConfirmationKey];
    if (passwordInput.value !== passwordConfirmationInput.value) {
      return passwordConfirmationInput.setErrors({mismatchedPasswords: true})
    }
  }
}
