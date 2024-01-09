import {Component} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {UserSettingsDto} from '../../../dtos/userSettingsDto';
import {AuthService} from '../../../services/auth.service';
import {PasswordEncoder} from '../../../utils/passwordEncoder';
import {Router} from '@angular/router';
import {UpdateAuthenticationSettingsDto} from '../../../dtos/updateAuthenticationSettingsDto';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-change-email',
  templateUrl: './change-email.component.html',
  styleUrls: ['./change-email.component.scss']
})
export class ChangeEmailComponent {

  settingsForm: UntypedFormGroup;
  submitted = false;
  error = false;
  errorMessage = '';

  originalUserSettings: UserSettingsDto;

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private passwordEncoder: PasswordEncoder, private router: Router, private notifications: ToastrService) {
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
        this.errorMessage = typeof error.error === 'object' ? error.error.error : error.error;
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
}
