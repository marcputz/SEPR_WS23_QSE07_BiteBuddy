import {Component} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {UserSettingsDto} from '../../../dtos/userSettingsDto';
import {AuthService} from '../../../services/auth.service';
import {PasswordEncoder} from '../../../utils/passwordEncoder';
import {Router} from '@angular/router';
import {UpdateUserSettingsDto} from '../../../dtos/updateUserSettingsDto';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent {

  settingsForm: UntypedFormGroup;
  submitted = false;
  error = false;
  errorMessage = '';

  originalUserSettings: UserSettingsDto;

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private passwordEncoder: PasswordEncoder, private router: Router, private notifications: ToastrService) {
    this.settingsForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      password2: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  vanishError() {
    this.error = false;
  }

  public updateUserSettings() {
    this.submitted = true;
    if (this.settingsForm.valid && (this.settingsForm.controls.password.value && this.settingsForm.controls.password.value === this.settingsForm.controls.password2.value)) {
      const currentPassword: string = this.passwordEncoder.encodePassword(this.settingsForm.controls.currentPassword.value);
      const newPassword: string = this.passwordEncoder.encodePassword(this.settingsForm.controls.password.value);

      const updateUserSettingsDto: UpdateUserSettingsDto = new UpdateUserSettingsDto(
        null, currentPassword, newPassword
      );

      this.authService.updateUser(updateUserSettingsDto).subscribe({
        next: () => {
          console.log('Password updated successfully');
          this.notifications.success('Password updated successfully');
        },
        error: error => {
          console.error('Error updating user settings', error);
          this.notifications.error('Error updating password');
        }
      });
    } else if (this.settingsForm.controls.password.value !== this.settingsForm.controls.password2.value) {
      console.log('Passwords do not match');
      this.notifications.error('Passwords do not match');
    } else {
      console.log('Invalid input');
      this.notifications.error('Invalid input');
    }
  }
}
