import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../services/user.service";
import {PasswordEncoder} from "../../../utils/passwordEncoder";
import {Router} from "@angular/router";
import {isBoolean} from "lodash";
import {ToastrService} from "ngx-toastr";

@Component({
  selector: 'app-request-password-reset',
  templateUrl: './request-password-reset.component.html',
  styleUrls: ['./request-password-reset.component.scss']
})
export class RequestPasswordResetComponent implements OnInit {

  emailForm: UntypedFormGroup;
  submitted: boolean = false;

  protected requestSent: boolean = false;
  protected requestSuccess: boolean = false;

  protected email: string | null;

  protected errorMessage: string | null;

  isInputFocused: {[key: string]: boolean } = {};

  constructor(private formBuilder: UntypedFormBuilder,
              private authService: UserService,
              private passwordEncoder: PasswordEncoder,
              private router: Router,
              private notifications: ToastrService) {
    this.emailForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  ngOnInit(): void {

  }

  /**
   * Update the input focus flag in order to show/hide the label on the input field
   */
  updateInputFocus(attribute: string) {
    this.isInputFocused[attribute] = this.emailForm.get(attribute).value !== '';
  }

  onSubmit() {
    this.submitted = true;

    if (this.emailForm.valid) {
      this.email = <string> this.emailForm.controls.email.value;
      this.requestSent = true;

      this.authService.requestPasswordReset(this.email).subscribe({
        next: data => {
          this.requestSuccess = true;
        },
        error: error => {
          console.error("Error requesting password reset", error);

          let errorObject;
          if (typeof error.error === 'object') {
            errorObject = error.error;
          } else {
            errorObject = error;
          }

          let status = errorObject.status;
          let message = errorObject.error;

          switch (status) {
            case 404:
              // user with email does not exist
              this.emailForm.controls['email'].setErrors({userNotFound: true});
              this.notifications.error("Account '" + this.email + "' does not exist");
              break;
            case 503:
              // something wrong with password reset service
              console.warn("Server can not send password reset email");
              this.notifications.error("Could not send email. Try again later!");
              break;
            default: this.notifications.error("Something went wrong on our side, sorry! Please try again later."); break;
          }
          this.requestSent = false;
        }
      })
    }
  }

}
