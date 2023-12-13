import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute, NavigationEnd, Router, RouterState} from "@angular/router";
import {ToastrService} from "ngx-toastr";

@Component({
  selector: 'app-settings-layout',
  templateUrl: './settings-layout.component.html',
  styleUrls: ['./settings-layout.component.scss']
})
export class SettingsLayoutComponent implements OnInit {

  accountEmail: string | null;
  accountUsername: string | null;

  activeNavItem: string = 'account';

  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute, private notification: ToastrService) {
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
          case '/settings/account': this.setActiveNavItem('account'); break;
          case '/settings/password': this.setActiveNavItem('password'); break;
          case '/settings/system': this.setActiveNavItem('system'); break;
          default: this.setActiveNavItem(e.url.substring(e.url.lastIndexOf(('/')) + 1, e.url.length)); break;
        }
      }
    });
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
