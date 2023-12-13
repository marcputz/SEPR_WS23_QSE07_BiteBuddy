import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {PasswordEncoder} from '../../utils/passwordEncoder';
import {Router} from '@angular/router';
import {UserSettingsDto} from '../../dtos/userSettingsDto';
import {UpdateUserSettingsDto} from '../../dtos/updateUserSettingsDto';
import {AuthService} from "../../services/auth.service";


@Component({
  selector: 'app-user-settings',
  templateUrl: './user-settings.component.html',
  styleUrls: ['./user-settings.component.scss']
})
export class UserSettingsComponent implements OnInit {

  settingsForm: UntypedFormGroup;
  submitted = false;
  error = false;
  errorMessage = '';

  originalUserSettings: UserSettingsDto;

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private passwordEncoder: PasswordEncoder, private router: Router) {
    this.settingsForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      currentPassword: ['', [Validators.required]],
      password: ['', [Validators.minLength(8)]],
      password2: ['', [Validators.minLength(8)]]
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
        // Don't set values for password fields
      },
      error: error => {
        console.error(`Error loading user settings`, error);
        this.error = true;
        this.errorMessage = typeof error.error === 'object' ? error.error.error : error.error;
      },
      complete: () => { }
    });
  }

  public updateUserSettings() {
    this.submitted = true;
    if (this.settingsForm.valid && (!this.settingsForm.controls.password.value || this.settingsForm.controls.password.value === this.settingsForm.controls.password2.value)) {
      const email: string = this.settingsForm.controls.email.value;
      const currentPassword: string = this.passwordEncoder.encodePassword(this.settingsForm.controls.currentPassword.value);
      let newPassword: string = '';
      if(this.settingsForm.controls.password.value) {
        newPassword = this.passwordEncoder.encodePassword(this.settingsForm.controls.password.value);
      }

      const updateUserSettingsDto: UpdateUserSettingsDto = new UpdateUserSettingsDto(
        email, currentPassword, newPassword
      );

      this.authService.updateUser(updateUserSettingsDto).subscribe({
        next: () => {
          console.log('User settings updated successfully');
          // Handle successful update here
        },
        error: error => {
          console.error('Error updating user settings', error);
          // Handle errors here
        }
      });
    } else if (this.settingsForm.controls.password.value !== this.settingsForm.controls.password2.value) {
      console.log('Passwords do not match');
    } else {
      console.log('Invalid input');
    }
  }
}
