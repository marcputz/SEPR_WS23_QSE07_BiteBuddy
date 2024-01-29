import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {UserSettingsDto} from '../../../dtos/userSettingsDto';
import {UserService} from '../../../services/user.service';
import {PasswordEncoder} from '../../../utils/passwordEncoder';
import {Router} from '@angular/router';
import {UpdateAuthenticationSettingsDto} from '../../../dtos/updateAuthenticationSettingsDto';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-change-email',
  templateUrl: './change-email.component.html',
  styleUrls: ['./change-email.component.scss']
})
export class ChangeEmailComponent implements OnInit {

  settingsForm: UntypedFormGroup;
  submitted = false;
  error = false;
  errorMessage = 'Something went wrong, no user settings found. Check if your backend is connected';

  isInputFocused: { [key: string]: boolean } = {};
  showPasswords: boolean = false;

  originalUserSettings: UserSettingsDto;

  constructor(private formBuilder: UntypedFormBuilder, private authService: UserService, private passwordEncoder: PasswordEncoder, private router: Router, private notifications: ToastrService) {
    this.settingsForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      currentPassword: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  vanishError() {
    this.error = false;
  }

  ngOnInit() {
    this.getUser();
  }

  private getUser() {
    this.authService.getUser().subscribe({
      next: (settings: UserSettingsDto) => {
        this.originalUserSettings = settings;
        this.settingsForm.controls['email'].setValue(settings.email);
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
    if (this.settingsForm.valid && (this.settingsForm.controls.email.value !== this.originalUserSettings.email)) {
      const email: string = this.settingsForm.controls.email.value;
      const currentPassword: string = this.passwordEncoder.encodePassword(this.settingsForm.controls.currentPassword.value);

      const updateUserSettingsDto: UpdateAuthenticationSettingsDto = new UpdateAuthenticationSettingsDto(
        email, currentPassword, null
      );

      this.authService.updateUserAuthentication(updateUserSettingsDto).subscribe({
        next: () => {
          console.log('User settings updated successfully');
          this.notifications.success('Email updated successfully');
          this.getUser();
          this.authService.triggerUpdate();
          this.submitted = false;
          this.settingsForm.controls['currentPassword'].setValue('');
        },
        error: error => {
          console.error('Error updating Email', error);
          let errorMessage = typeof error.error === 'object' ? error.error.error : error.error;
          this.notifications.error(errorMessage, 'Error updating Email: ');
          if (errorMessage.indexOf("Password not valid") >= 0) {
            this.settingsForm.controls['currentPassword'].setErrors({valid: true});
          }
          if (errorMessage.indexOf("is already in use") >= 0) {
            this.settingsForm.controls['email'].setErrors({valid: true});
          }
        }
      });
    } else if (this.settingsForm.controls.email.value === this.originalUserSettings.email) {
      console.log('Email was not changed');
      this.notifications.error('Email was not changed');
      this.settingsForm.controls['email'].setErrors({changed: true});
    } else {
      console.log('Invalid input');
    }
  }

  togglePasswordVisibility() {
    this.showPasswords = !this.showPasswords;
  }

  /**
   * Update the input focus flag in order to show/hide the label on the input field
   */
  updateInputFocus(attribute: string) {
    this.isInputFocused[attribute] = this.settingsForm.get(attribute).value !== '';
  }
}
