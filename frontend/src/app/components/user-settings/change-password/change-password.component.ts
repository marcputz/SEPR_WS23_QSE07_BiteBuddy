import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {UserService} from '../../../services/user.service';
import {PasswordEncoder} from '../../../utils/passwordEncoder';
import {Router} from '@angular/router';
import {UpdateAuthenticationSettingsDto} from '../../../dtos/updateAuthenticationSettingsDto';
import {ToastrService} from 'ngx-toastr';
import {ErrorHandler} from '../../../services/errorHandler';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnInit {
  settingsForm: UntypedFormGroup;
  submitted = false;
  error = false;

  isInputFocused: { [key: string]: boolean } = {};
  showPasswords: boolean = false;
  showPassword1: boolean = false;
  showPassword2: boolean = false;
  authError: boolean;

  constructor(private formBuilder: UntypedFormBuilder, private authService: UserService, private passwordEncoder: PasswordEncoder, private router: Router, private notifications: ToastrService, private errorHandler: ErrorHandler,
  ) {
    this.settingsForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required, Validators.minLength(8)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      password2: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  ngOnInit(): void {
  }

  vanishError() {
    this.error = false;
  }

  public updateUserSettings() {
    this.submitted = true;
    if (this.settingsForm.valid && (this.settingsForm.controls.password.value && this.settingsForm.controls.password.value === this.settingsForm.controls.password2.value)) {
      const currentPassword: string = this.passwordEncoder.encodePassword(this.settingsForm.controls.currentPassword.value);
      const newPassword: string = this.passwordEncoder.encodePassword(this.settingsForm.controls.password.value);

      const updateUserSettingsDto: UpdateAuthenticationSettingsDto = new UpdateAuthenticationSettingsDto(
        null, currentPassword, newPassword
      );

      this.authService.updateUserAuthentication(updateUserSettingsDto).subscribe({
        next: () => {
          console.log('Password updated successfully');
          this.notifications.success('Password updated successfully');
          this.submitted = false;
          this.settingsForm.controls['currentPassword'].setValue('');
          this.settingsForm.controls['password'].setValue('');
          this.settingsForm.controls['password2'].setValue('');
        },
        error: error => {
          let errorObj = this.errorHandler.getErrorObject(error);

          if (errorObj.status == 401) {
            // unauthorized -> invalid credentials
            console.warn("Invalid Credentials");
            this.authError = true;
            this.notifications.warning("Wrong password");
          } else {
            this.errorHandler.handleApiError(errorObj);
          }
        }
      });
    } else if (this.settingsForm.controls.password.value !== this.settingsForm.controls.password2.value) {
      console.log('Passwords do not match');
      this.notifications.error('Passwords do not match');
      this.settingsForm.controls['password'].setErrors({match: true});
      this.settingsForm.controls['password2'].setErrors({match: true});

    } else {
      console.log('Invalid input');
      this.notifications.error('Invalid input');
    }
  }

  togglePasswordVisibility() {
    this.showPasswords = !this.showPasswords;
  }

  togglePasswordVisibility1() {
    this.showPassword1 = !this.showPassword1;
  }

  togglePasswordVisibility2() {
    this.showPassword2 = !this.showPassword2;
  }

  /**
   * Update the input focus flag in order to show/hide the label on the input field
   */
  updateInputFocus(attribute: string) {
    this.isInputFocused[attribute] = this.settingsForm.get(attribute).value !== '';
  }
}
