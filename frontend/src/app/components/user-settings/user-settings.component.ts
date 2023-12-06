import { Component } from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {UserService} from '../../services/user.service';
import {PasswordEncoder} from '../../utils/passwordEncoder';
import {Router} from '@angular/router';
import {UserSettingsDto} from '../../dtos/userSettingsDto';
import {UpdateUserSettingsDto} from '../../dtos/updateUserSettingsDto';

@Component({
  selector: 'app-user-settings',
  templateUrl: './user-settings.component.html',
  styleUrls: ['./user-settings.component.scss']
})
export class UserSettingsComponent {

  settingsForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  // Error flag
  error = false;
  errorMessage = '';

  originalUserSettings: UserSettingsDto;

  constructor(private formBuilder: UntypedFormBuilder, private authService: UserService, private passwordEncoder: PasswordEncoder, private router: Router) {
    this.settingsForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      password2: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  /**
   * Error flag will be deactivated, which clears the error message
   */
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

        // Set form controls with the loaded data
        this.settingsForm.controls['username'].setValue(settings.nickname);
        this.settingsForm.controls['email'].setValue(settings.email);

        this.settingsForm.controls['password'].setValue('');
        this.settingsForm.controls['password2'].setValue('');
      },
      error: error => {
        console.error(`Error loading user settings`, error);
        this.error = true;
        if (typeof error.error === 'object') {
          this.errorMessage = error.error.error;
        } else {
          this.errorMessage = error.error;
        }
      },
      complete: () => {
        // Any additional actions on completion
      }
    });
  }

  public updateUserSettings() {
    this.submitted = true;
    if (this.settingsForm.valid && this.settingsForm.controls.password.value === this.settingsForm.controls.password2.value) {
      const nickname: string = this.settingsForm.controls.username.value;
      const email: string = this.settingsForm.controls.email.value;
      const password: string = this.passwordEncoder.encodePassword(this.settingsForm.controls.password.value);

      // Create DTO for update operation
      const updateUserSettingsDto: UpdateUserSettingsDto = new UpdateUserSettingsDto(email, nickname, password);

      this.authService.updateUser(updateUserSettingsDto).subscribe({
        next: (response) => {
          console.log('User settings updated successfully');
          // Handle successful update here, e.g., navigate to a different page or show a success message
        },
        error: error => {
          console.error('Error updating user settings', error);
          // Handle errors here, e.g., show an error message
        }
      });
    } else if (this.settingsForm.controls.password.value !== this.settingsForm.controls.password2.value) {
      console.log('Passwords do not match');
    } else {
      console.log('Invalid input');
    }
  }
}
