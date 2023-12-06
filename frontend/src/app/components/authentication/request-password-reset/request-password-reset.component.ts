import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../../services/auth.service";
import {PasswordEncoder} from "../../../utils/passwordEncoder";
import {Router} from "@angular/router";
import {isBoolean} from "lodash";

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
  protected requestError: boolean = false;

  protected email: string | null;

  protected errorMessage: string | null;

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private passwordEncoder: PasswordEncoder, private router: Router) {
    this.emailForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  ngOnInit(): void {

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
          this.requestError = true;

          let status = 500;
          let message: string | null;
          if (typeof error.error === 'object') {
            status = error.error.status;
            message = error.error.message;
          } else {
            status = error.status;
            message = error.message;
          }

          switch (status) {
            case 404: this.errorMessage = message; break;
            case 401: this.errorMessage = message; break;
            default: this.errorMessage = "Something went wrong on our side, sorry! Please try again later."; break;
          }

        }
      })
    }
  }

}
