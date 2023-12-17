import {NgModule} from '@angular/core';
import {mapToCanActivate, RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/dialogs/login/login.component';
import {AuthGuard} from './guards/auth.guard';
import {MessageComponent} from './components/message/message.component';
import {RegisterComponent} from "./components/authentication/register/register.component";
import {
  RequestPasswordResetComponent
} from "./components/dialogs/request-password-reset/request-password-reset.component";
import {PasswordResetComponent} from "./components/dialogs/password-reset/password-reset.component";
import {RecipeListComponent} from "./components/recipe-list/recipe-list.component";
import {RecipeDetailComponent} from "./components/recipe-detail/recipe-detail.component";
import {LandingLayoutComponent} from "./layouts/landing-layout/landing-layout.component";
import {DialogLayoutComponent} from "./layouts/dialog-layout/dialog-layout.component";
import {ChangeSettingsComponent} from './components/user-settings/change-settings/change-settings.component';
import {ChangeEmailComponent} from './components/user-settings/change-email/change-email.component';
import {ChangePasswordComponent} from './components/user-settings/change-password/change-password.component';

const routes: Routes = [
  //{path: '', component: HomeComponent},

  {path: '*', redirectTo: ''}, // Redirection for unknown paths

  {path: '', component: LandingLayoutComponent}, // Landing Page
  {
    path: '', component: DialogLayoutComponent, children: [ // Pages using Dialog Box Layout
      {path: 'login', component: LoginComponent}, // Login Page
      {path: 'request_password_reset', component: RequestPasswordResetComponent}, // Forgot password page
      {path: 'password_reset', component: PasswordResetComponent}, // Password reset page
    ]
  },

  // {path: 'login', component: LoginComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'account', component: ChangeSettingsComponent},
  {path: 'email', component: ChangeEmailComponent},
  {path: 'password', component: ChangePasswordComponent},
  {path: 'message', canActivate: mapToCanActivate([AuthGuard]), component: MessageComponent},
  {
    path: 'recipes', children: [
      {path: '', component: RecipeListComponent},
      {path: ':id', component: RecipeDetailComponent}
    ]
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: false})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
