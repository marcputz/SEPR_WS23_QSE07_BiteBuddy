import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {UserSettingsDto} from '../../../dtos/userSettingsDto';
import {UserService} from '../../../services/user.service';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {UpdateUserSettingsDto} from '../../../dtos/updateUserSettingsDto';
import {SafeUrl} from '@angular/platform-browser';
import {ImageHandler} from '../../../utils/imageHandler';

@Component({
  selector: 'app-change-settings',
  templateUrl: './change-settings.component.html',
  styleUrls: ['./change-settings.component.scss']
})
export class ChangeSettingsComponent implements OnInit {

  settingsForm: UntypedFormGroup;
  submitted = false;
  error = false;
  errorMessage = 'Something went wrong, no user settings found. Check if your backend is connected';

  isInputFocused: {[key: string]: boolean } = {};

  originalUserSettings: UserSettingsDto;
  newUserSettings: UpdateUserSettingsDto = new UpdateUserSettingsDto("", null);

  pictureSelected: File = null;
  safePictureUrl: SafeUrl = '/assets/icons/user_default.png';

  constructor(
    private formBuilder: UntypedFormBuilder,
    private authService: UserService,
    private router: Router,
    private notifications: ToastrService,
    private imageHandler: ImageHandler
  ) {
    this.settingsForm = this.formBuilder.group({
      nickname: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(255)]]
    });
  }

  vanishError() {
    this.error = false;
  }

  ngOnInit() {
    this.getUser();
  }

  onPictureChange(event) {
    if (event.target.files.length > 0) {
      this.imageHandler.prepareUserPicture(event.target.files[0])
        .then(imageBytes => {
          this.newUserSettings.userPicture = imageBytes;
          this.loadPreviewPicture();
        })
        .catch(error => {
          console.error('Error processing image: ', error);
          // Handle the error appropriately
        });
    } else {
      this.newUserSettings.userPicture = null;
      this.loadPreviewPicture();
    }
  }


  @ViewChild('fileInput') fileInput: ElementRef;

  triggerFileInput() {
    this.fileInput.nativeElement.click();
  }

  private getUser() {
    this.authService.getUser().subscribe({
      next: (settings: UserSettingsDto) => {
        this.originalUserSettings = settings;
        this.settingsForm.controls['nickname'].setValue(settings.nickname);
        this.loadUserPicture(settings.userPicture);
        this.newUserSettings = new UpdateUserSettingsDto("", null);
      },
      error: error => {
        console.error('Error loading user settings');
        this.notifications.error('Error loading user settings');

        this.error = true;
        this.errorMessage = typeof error.error === 'object' ? error.error.error : error.error;
      },
      complete: () => {
      }
    },

    );
  }

  loadUserPicture(userPictureArray: number[]) {
    if (userPictureArray === undefined) {
      console.info('user picture is empty');
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(this.safePictureUrl);
    } else {
      console.info('user picture loaded');
      //this.loadPreviewPicture();
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(userPictureArray);
    }
  }

  loadPreviewPicture() {
    if (this.newUserSettings.userPicture === null) {
      console.info('loadProfilePicture originalUserSettings');
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(this.originalUserSettings.userPicture);
    } else {
      console.info('loadProfilePicture newUserSettings');
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(btoa(String.fromCharCode.apply(null, new Uint8Array(this.newUserSettings.userPicture))));
    }
  }

  public updateUserSettings() {
    this.submitted = true;
    let somethingChanged = false;
    if (this.settingsForm.valid) {
      if (this.settingsForm.controls.nickname.value !== this.originalUserSettings.nickname) {
        const nickname: string = this.settingsForm.controls.nickname.value;
        this.newUserSettings.nickname = nickname;
        somethingChanged = true;
      }
      if (this.newUserSettings.userPicture !== null) {
        somethingChanged = true;
      }
      if (somethingChanged) {
        this.authService.updateUserSettings(this.newUserSettings).subscribe({
          next: () => {
            this.notifications.success('User settings updated successfully');
            this.getUser();
            this.authService.triggerUpdate();
          },
          error: error => {
            console.error('Error updating user settings', error);
            this.errorMessage = typeof error.error === 'object' ? error.error.error : error.error;

            this.notifications.error(this.errorMessage, 'Error loading user settings: ');
          }
        });
      } else {
        this.notifications.success('No changes made');
        console.log('No changes to User Settings');
      }
    } else {
      console.log('Invalid input');
    }
  }

  /**
   * Update the input focus flag in order to show/hide the label on the input field
   */
  updateInputFocus(attribute: string) {
    this.isInputFocused[attribute] = this.settingsForm.get(attribute).value !== '';
  }
}
