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
import {ToastrService} from "ngx-toastr";
import {split, toNumber} from "lodash";

@Component({
  selector: 'app-password-reset',
  templateUrl: './password-reset.component.html',
  styleUrls: ['./password-reset.component.scss']
})
export class PasswordResetComponent implements OnInit {

  passwordForm: UntypedFormGroup;
  submitted: boolean = false;

  showPasswords: boolean = false;

  urlValid: boolean;
  requestIdValid: boolean;
  expDateValid: boolean;

  requestSent: boolean = false;
  requestSuccess: boolean = false;

  protected requestId: string | null;
  protected expirationDate: Date | null;

  constructor(private formBuilder: UntypedFormBuilder,
              private authService: AuthService,
              private passwordEncoder: PasswordEncoder,
              private router: Router,
              private activatedRoute: ActivatedRoute,
              private notifications: ToastrService) {
    this.passwordForm = this.formBuilder.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      passwordConfirm: ['', [Validators.required, Validators.minLength(8)]],
    }, {validator: matchingPasswords('password', 'passwordConfirm')});
  }

  ngOnInit(): void {
    this.requestId = this.activatedRoute.snapshot.queryParamMap.get('id');
    const expDateString = this.activatedRoute.snapshot.queryParamMap.get('exp');

    /* check URL parameters */

    // check request ID
    if (this.requestId == undefined) {
      this.requestIdValid = false;
    } else {
      this.requestIdValid = true;
    }

    // parse and check date
    this.expDateValid = false;
    const dateStringParts = expDateString.split('T');
    if (dateStringParts.length == 2) {
      const dateParts = dateStringParts[0].split('-');
      const timeParts = dateStringParts[1].split('.');
      if (dateParts.length == 3 && timeParts.length == 2) {
        const year = toNumber(dateParts[0]);
        const month = toNumber(dateParts[1]);
        const day = toNumber(dateParts[2]);
        const hour = toNumber(timeParts[0]);
        const minutes = toNumber(timeParts[1]);
        const expDate = new Date(year, month-1, day, hour, minutes);
        this.expirationDate = expDate;
        if (expDate.getTime() > new Date().getTime()) {
          this.expDateValid = true;
        }
      }
    }

    this.urlValid = this.requestIdValid && this.expDateValid;
  }

  onSubmit() {
    this.submitted = true;
    if (this.passwordForm.valid) {
      if (this.urlValid) {

        const password: string = <string>this.passwordForm.controls.password.value;
        const encodedPassword = this.passwordEncoder.encodePassword(password);

        let dto = new ResetPasswordDto(this.requestId, encodedPassword);

        this.requestSent = true;
        this.authService.resetPassword(dto).subscribe({
          next: data => {
            this.requestSuccess = true;
          },
          error: error => {
            console.error("Could not reset password", error);

            let errorObject;
            if (typeof error.error === 'object') {
              errorObject = error.error;
            } else {
              errorObject = error;
            }

            let status = errorObject.status;
            let message = errorObject.error;

            switch (status) {
              case 400:
                // bad request, validation error
                this.notifications.error("New password does not match requirements");
                break;
              case 401:
                // unauthorized, invalid request id
                this.notifications.error("This request is invalid. Please try again!");
                break;
              default:
                this.notifications.error("Something went wrong on our side, sorry! Please try again later.");
                break;
            }
            this.requestSent = false;
          }
        });

      } else {
        this.notifications.error("Invalid Request URL");
      }
    }
  }

  togglePasswordHide() {
    this.showPasswords = !this.showPasswords;
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
