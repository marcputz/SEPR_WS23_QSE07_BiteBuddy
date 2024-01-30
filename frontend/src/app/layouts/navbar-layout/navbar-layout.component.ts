import {Component, OnInit} from '@angular/core';
import {UserService} from "../../services/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {UserSettingsDto} from "../../dtos/userSettingsDto";
import {ImageHandler} from "../../utils/imageHandler";
import { SafeUrl } from '@angular/platform-browser';
import {ErrorHandler} from "../../services/errorHandler";


@Component({
  selector: 'app-navbar-layout',
  templateUrl: './navbar-layout.component.html',
  styleUrls: ['./navbar-layout.component.scss']
})
export class NavbarLayoutComponent implements OnInit {

  showSecondaryNavbar: { [key: string]: boolean } = {
    recipes: false,
    profiles: false
  };

  userSettings: UserSettingsDto;

  accountUsername: string = '';

  safePictureUrl: SafeUrl = '/assets/icons/user_default.png';

  error = false;

  constructor(private authService: UserService,
              private errorHandler: ErrorHandler,
              private router: Router,
              private route: ActivatedRoute,
              private notification: ToastrService,
              private imageHandler: ImageHandler) {
  }

  ngOnInit(): void {
    //retrieve the username + image
    this.getUser();
  }

  toggleSecondaryNavbar(item: string) {
    // Close other secondary navbars before opening the selected one
    Object.keys(this.showSecondaryNavbar).forEach(key => {
      if (key !== item) {
        this.showSecondaryNavbar[key] = false;
      }
    });

    this.showSecondaryNavbar[item] = !this.showSecondaryNavbar[item];
  }

  private getUser() {
    this.authService.getUser().subscribe({
      next: (userSettingsDto: UserSettingsDto) => {
        this.accountUsername = userSettingsDto.nickname
        this.loadUserPicture(userSettingsDto.userPicture);
      },
      error: error => {

        let errorObj = this.errorHandler.getErrorObject(error);
        this.errorHandler.handleApiError(errorObj);

      },
      complete: () => {
      }
    });
  }

  loadUserPicture(userPictureArray: number[]) {
    if (userPictureArray === undefined) {
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(this.userSettings.userPicture);
    } else {
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(userPictureArray);
    }
  }

  onClickLogout() {
    this.authService.logoutUser();
    this.router.navigate(['/']);
  }

  onClickSettings() {
    this.router.navigate(['/settings']);
  }
}
