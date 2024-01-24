import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute, NavigationEnd, Router, RouterState} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {BreakpointObserver, Breakpoints} from "@angular/cdk/layout";
import {SafeUrl} from "@angular/platform-browser";
import {ImageHandler} from "../../utils/imageHandler";
import {UserSettingsDto} from "../../dtos/userSettingsDto";

@Component({
  selector: 'app-settings-layout',
  templateUrl: './settings-layout.component.html',
  styleUrls: ['./settings-layout.component.scss']
})
export class SettingsLayoutComponent implements OnInit {

  accountEmail: string | null;
  accountUsername: string | null;

  isTablet: boolean = false;
  isPhone: boolean = false;

  activeNavItem: string = 'account';

  safePictureUrl: SafeUrl = '/assets/icons/user_default.png';

  error = false;
  errorMessage = '';

  userSettings: UserSettingsDto;

  constructor(private authService: AuthService,
              private router: Router,
              private route: ActivatedRoute,
              private notification: ToastrService,
              private responsive: BreakpointObserver,
              private imageHandler: ImageHandler) {
    this.accountEmail = null;
    this.accountUsername = null;
  }

  ngOnInit(): void {
      // retrieve user account info
      this.accountUsername = this.authService.getTokenUsername(this.authService.getToken());
      if (this.accountUsername == undefined) {
        console.warn("Could not retrieve username from authentication token");
        this.notification.error("Could not retrieve username");
        this.accountUsername = "Username";
      }
      this.accountEmail = this.authService.getTokenEmail(this.authService.getToken());
    if (this.accountEmail == undefined) {
      console.warn("Could not retrieve email from authentication token");
      this.notification.error("Could not retrieve email");
      this.accountEmail = "Email";
    }

    // get active child via route
    this.router.events.subscribe((e) => {
      if (e instanceof NavigationEnd) {
        // route changed
        switch (e.url) {
          case '/settings/user': this.setActiveNavItem('user'); break;
          case '/settings/email': this.setActiveNavItem('email'); break;
          case '/settings/password': this.setActiveNavItem('password'); break;
          default: this.setActiveNavItem(e.url.substring(e.url.lastIndexOf(('/')) + 1, e.url.length)); break;
        }
      }
    });

    this.responsive.observe(['(max-width: 991px) and (min-width: 576px)']).subscribe(state => {
      this.isTablet = state.matches;
    })
    this.responsive.observe(['(max-width: 575px)']).subscribe(state => {
      this.isPhone = state.matches;
    })

    //retrieve then the user image
    this.getUser();
  }

  setActiveNavItem(itemLabel: string) {
    this.activeNavItem = itemLabel;
  }

  onClickLogout() {
    this.authService.logoutUser();
    this.router.navigate(['/']);
  }

  onClickDashboard() {
    this.router.navigate(['/']);
  }

  private getUser() {
    this.authService.getUser().subscribe({
      next: (userSettingsDto: UserSettingsDto) => {
        this.loadUserPicture(userSettingsDto.userPicture);
      },
      error: error => {
        console.error('Error loading user infos');
        this.notification.error('Error loading user infos');

        this.error = true;
        this.errorMessage = typeof error.error === 'object' ? error.error.error : error.error;
      },
      complete: () => {
      }
    });
  }

  loadUserPicture(userPictureArray: number[]) {
    console.info('load user picture');
    if (userPictureArray === undefined) {
      console.info('user picture is empty');
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(this.userSettings.userPicture);
    } else {
      console.info('user picture loaded');
      this.safePictureUrl = this.imageHandler.sanitizeUserImage(userPictureArray);
    }
  }

}
