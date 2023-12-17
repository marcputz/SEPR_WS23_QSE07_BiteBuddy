import {Component} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {UserSettingsDto} from '../../../dtos/userSettingsDto';
import {AuthService} from '../../../services/auth.service';
import {PasswordEncoder} from '../../../utils/passwordEncoder';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-change-settings',
  templateUrl: './change-settings.component.html',
  styleUrls: ['./change-settings.component.scss']
})
export class ChangeSettingsComponent {

  settingsForm: UntypedFormGroup;
  submitted = false;
  error = false;
  errorMessage = '';

  originalUserSettings: UserSettingsDto;

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private passwordEncoder: PasswordEncoder, private router: Router, private notifications: ToastrService) {
    this.settingsForm = this.formBuilder.group({
      nickname: ['', [Validators.required, Validators.maxLength(255)]]
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
        this.settingsForm.controls['nickname'].setValue(settings.nickname);
        // Don't set values for password fields
      },
      error: error => {
        console.error('Error loading user settings', error);
        this.notifications.error('Error loading user settings');
        this.error = true;
        this.errorMessage = typeof error.error === 'object' ? error.error.error : error.error;
      },
      complete: () => {
      }
    });
  }

  public updateUserSettings() {
    this.submitted = true;
    if (this.settingsForm.valid) {
      const nickname: string = this.settingsForm.controls.nickname.value;
      //const updateUserSettingsDto: UpdateUserSettingsDto = new UpdateNicknameDto(
      //  nickname
      //);

      /*this.authService.updateUser(updateUserSettingsDto).subscribe({
        next: () => {
          console.log('User settings updated successfully');
          this.notifications.success('User settings updated successfully');
          this.getUser();
        },
        error: error => {
          console.error('Error updating user settings', error);
          this.notifications.error('Error updating user settings');
        }
      });*/
    } else {
      console.log('Invalid input');
    }
  }
}
