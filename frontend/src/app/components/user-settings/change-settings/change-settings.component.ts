import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {
  AbstractControl,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {UserSettingsDto} from '../../../dtos/userSettingsDto';
import {UserService} from '../../../services/user.service';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {UpdateUserSettingsDto} from '../../../dtos/updateUserSettingsDto';
import {SafeUrl} from '@angular/platform-browser';
import {ImageHandler} from '../../../utils/imageHandler';
import {ErrorHandler} from "../../../services/errorHandler";

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

  isInputFocused: { [key: string]: boolean } = {};

  originalUserSettings: UserSettingsDto;
  newUserSettings: UpdateUserSettingsDto = new UpdateUserSettingsDto("", null);

  pictureSelected: File = null;
  safePictureUrl: SafeUrl = '/assets/icons/user_default.png';

  constructor(
    private formBuilder: UntypedFormBuilder,
    private authService: UserService,
    private errorHandler: ErrorHandler,
    private router: Router,
    private notifications: ToastrService,
    private imageHandler: ImageHandler
  ) {
    this.settingsForm = this.formBuilder.group({
      nickname: ['', [Validators.required, this.trimmedMinLength(3), Validators.maxLength(255)]]
    });
  }


  trimmedMinLength(minLength: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value || '';
      const trimmedValue = value.trim();
      return trimmedValue.length < minLength ? {
        'trimmedMinLength': {
          requiredLength: minLength,
          actualLength: trimmedValue.length
        }
      } : null;
    };
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
          this.notifications.error(error, 'Unsupported Format use png or Jpg');
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

          let errorObj = this.errorHandler.getErrorObject(error);
          this.errorHandler.handleApiError(errorObj);

        },
        complete: () => {
        }
      },
    );
  }

  loadUserPicture(userPictureArray: number[]) {
    if (userPictureArray === undefined) {
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(this.safePictureUrl);
    } else {
      //this.loadPreviewPicture();
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(userPictureArray);
    }
  }

  loadPreviewPicture() {
    if (this.newUserSettings.userPicture === null) {
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(this.originalUserSettings.userPicture);
    } else {
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(btoa(String.fromCharCode.apply(null, new Uint8Array(this.newUserSettings.userPicture))));
    }
  }

  public updateUserSettings() {
    this.submitted = true;
    let somethingChanged = false;
    if (this.settingsForm.valid) {
      if (this.settingsForm.controls.nickname.value !== this.originalUserSettings.nickname) {
        this.newUserSettings.nickname = this.settingsForm.controls.nickname.value;
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
      }
    } else {
      this.notifications.success('Invalid input');
    }
  }

  /**
   * Update the input focus flag in order to show/hide the label on the input field
   */
  updateInputFocus(attribute: string) {
    this.isInputFocused[attribute] = this.settingsForm.get(attribute).value !== '';
  }
}
