import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {UserSettingsDto} from '../../../dtos/userSettingsDto';
import {UserService} from '../../../services/user.service';
import {PasswordEncoder} from '../../../utils/passwordEncoder';
import {Router} from '@angular/router';
import {UpdateAuthenticationSettingsDto} from '../../../dtos/updateAuthenticationSettingsDto';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnInit {

  settingsForm: UntypedFormGroup;
  submitted = false;
  error = false;
  errorMessage = 'Something went wrong, no user settings found. Check if your backend is connected';

  isInputFocused: {[key: string]: boolean } = {};
  showPasswords: boolean = false;
  showPassword1: boolean = false;
  showPassword2: boolean = false;

  originalUserSettings: UserSettingsDto;

  constructor(private formBuilder: UntypedFormBuilder, private authService: UserService, private passwordEncoder: PasswordEncoder, private router: Router, private notifications: ToastrService) {
    this.settingsForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required, Validators.minLength(8)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      password2: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  vanishError() {
    this.error = false;
  }

  ngOnInit(): void {
    this.getUser();
  }

  private getUser() {
    this.authService.getUser().subscribe({
      next: (settings: UserSettingsDto) => {
        this.originalUserSettings = settings;
      },
      error: error => {
        console.error('Error loading user settings', error);
        this.notifications.error('Error loading user settings, try again');
        this.error = true;
        //TODO: not working when server down this.errorMessage = typeof error.error === 'object' ? error.error.error : error.error;
      },
      complete: () => {
      }
    });
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
          console.error('Error updating Password', error);
          let errorMessage = typeof error.error === 'object' ? error.error.error : error.error;
          this.notifications.error(errorMessage, 'Error updating Password: ');
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
