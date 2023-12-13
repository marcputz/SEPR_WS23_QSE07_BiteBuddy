import {NgModule} from '@angular/core';
import {mapToCanActivate, RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/dialogs/login/login.component';
import {AuthGuard} from './guards/auth.guard';
import {MessageComponent} from './components/message/message.component';
import {UserSettingsComponent} from './components/user-settings/user-settings.component';
import {RegisterComponent} from "./components/authentication/register/register.component";
import {
  RequestPasswordResetComponent
} from "./components/dialogs/request-password-reset/request-password-reset.component";
import {PasswordResetComponent} from "./components/dialogs/password-reset/password-reset.component";
import {RecipeListComponent} from "./components/recipe-list/recipe-list.component";
import {RecipeDetailComponent} from "./components/recipe-detail/recipe-detail.component";
import {LandingLayoutComponent} from "./layouts/landing-layout/landing-layout.component";
import {DialogLayoutComponent} from "./layouts/dialog-layout/dialog-layout.component";
import {SettingsLayoutComponent} from "./layouts/settings-layout/settings-layout.component";

const routes: Routes = [
  //{path: '', component: HomeComponent},

  {path: '*', redirectTo: ''}, // Redirection for unknown paths

  {path: '', canActivate: mapToCanActivate([AuthGuard]), component: LandingLayoutComponent}, // Landing Page
  {path: '', component: DialogLayoutComponent, children: [ // Pages using Dialog Box Layout
      {path: 'login', component: LoginComponent}, // Login Page
      {path: 'request_password_reset', component: RequestPasswordResetComponent}, // Forgot password page
      {path: 'password_reset', component: PasswordResetComponent}, // Password reset page
  ]},
  {path: 'dashboard', canActivate: mapToCanActivate([AuthGuard]), component: MessageComponent},
  {path: 'settings', component: SettingsLayoutComponent, children: [
      {path: '', component: LoginComponent},
      //{path: '', pathMatch: 'full', redirectTo: 'account'},
      {path: 'account', component: UserSettingsComponent},
      {path: 'password', component: UserSettingsComponent},
      {path: 'system', component: LoginComponent},
  ]},

  {path: 'register', component: RegisterComponent},
  {path: 'message', canActivate: mapToCanActivate([AuthGuard]), component: MessageComponent},
  {path: 'recipes', children: [
      {path: '', component: RecipeListComponent},
      {path: ':id', component: RecipeDetailComponent}
  ]},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: false})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
