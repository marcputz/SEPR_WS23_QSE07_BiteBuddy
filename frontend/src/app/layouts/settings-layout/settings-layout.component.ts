import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute, NavigationEnd, Router, RouterState} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {BreakpointObserver, Breakpoints} from "@angular/cdk/layout";

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

  constructor(private authService: AuthService,
              private router: Router,
              private route: ActivatedRoute,
              private notification: ToastrService,
              private responsive: BreakpointObserver) {
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

}
