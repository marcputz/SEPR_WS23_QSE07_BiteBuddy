import {Component, OnInit} from '@angular/core';
import {
  FormGroup,
  UntypedFormBuilder,
  UntypedFormGroup,
  Validators
} from "@angular/forms";
import {UserService} from "../../../services/user.service";
import {PasswordEncoder} from "../../../utils/passwordEncoder";
import {ActivatedRoute, Router} from "@angular/router";
import {ResetPasswordDto} from "../../../dtos/resetPasswordDto";
import {ToastrService} from "ngx-toastr";
import {toNumber} from "lodash";
import {ErrorHandler} from "../../../services/errorHandler";

@Component({
  selector: 'app-password-reset',
  templateUrl: './password-reset.component.html',
  styleUrls: ['./password-reset.component.scss']
})
export class PasswordResetComponent implements OnInit {

  passwordForm: UntypedFormGroup;
  submitted: boolean = false;

  showPasswords: boolean = false;
  showPassword1: boolean = false;

  urlValid: boolean;
  requestIdValid: boolean;
  expDateValid: boolean;

  requestSent: boolean = false;
  requestSuccess: boolean = false;

  isInputFocused: {[key: string]: boolean } = {};

  protected requestId: string | null;
  protected expirationDate: Date | null;

  constructor(private formBuilder: UntypedFormBuilder,
              private authService: UserService,
              private passwordEncoder: PasswordEncoder,
              private errorHandler: ErrorHandler,
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
    this.requestIdValid = this.requestId != undefined;

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

            let errorObj = this.errorHandler.getErrorObject(error);

            switch (errorObj.status) {
              case 400:
                // validation error
                this.notifications.error("New Password does not match the requirements");
                break;
              case 401:
                this.notifications.error("This Request ID is invalid, please try again!");
                break;
              default:
                this.errorHandler.handleApiError(errorObj);
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
    this.isInputFocused[attribute] = this.passwordForm.get(attribute).value !== '';
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
