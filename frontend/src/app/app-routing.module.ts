import {NgModule} from '@angular/core';
import {mapToCanActivate, RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './components/dialogs/login/login.component';
import {AuthGuard} from './guards/auth.guard';
import {RegisterComponent} from "./components/dialogs/register/register.component";
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
import {SettingsLayoutComponent} from "./layouts/settings-layout/settings-layout.component";
import {RecipeCreateComponent} from "./components/recipe-create/recipe-create.component";
import {ProfileComponent} from "./components/dialogs/profile/profile.component";
import {MenuPlanComponent} from "./components/menu-plan/menu-plan.component";
import {InventoryComponent} from "./components/inventory/inventory.component";
import {MenuPlanLookupComponent} from "./components/menu-plan-lookup/menu-plan-lookup.component";
import {NavbarLayoutComponent} from "./layouts/navbar-layout/navbar-layout.component";

const routes: Routes = [
  {path: '*', redirectTo: ''}, // Redirection for unknown paths

  {path: '', canActivate: mapToCanActivate([AuthGuard]), component: LandingLayoutComponent}, // Landing Page
  {path: '', component: DialogLayoutComponent, children: [ // Pages using Dialog Box Layout
      {path: 'login', component: LoginComponent}, // Login Page
      {path: 'request_password_reset', component: RequestPasswordResetComponent}, // Forgot password page
      {path: 'password_reset', component: PasswordResetComponent}, // Password reset page
      {path: 'profile', component: ProfileComponent}
    ]},
  {path: 'register', component: RegisterComponent}, // Register Page
  {path: '', component: NavbarLayoutComponent, children: [ // Pages using Navbar Layout
      {path: 'dashboard', canActivate: mapToCanActivate([AuthGuard]), component: RecipeListComponent}, // TODO: add dashboard component
      {path: 'recipes', children: [
          {path: '', component: RecipeListComponent},
          {path: 'create', component: RecipeCreateComponent},
          {path: ':id', component: RecipeDetailComponent},
        ]},
    ]},
  {path: 'settings', canActivate: mapToCanActivate([AuthGuard]), component: SettingsLayoutComponent, children: [
      {path: '', pathMatch: 'full', redirectTo: 'user'},
      {path: 'user', canActivate: mapToCanActivate([AuthGuard]), component: ChangeSettingsComponent},
      {path: 'email', canActivate: mapToCanActivate([AuthGuard]), component: ChangeEmailComponent},
      {path: 'password', canActivate: mapToCanActivate([AuthGuard]), component: ChangePasswordComponent},
  ]},
  {path: 'menuplan', canActivate: mapToCanActivate([AuthGuard]), component: MenuPlanComponent},
  {path: 'menuplanLookup', canActivate: mapToCanActivate([AuthGuard]), component: MenuPlanLookupComponent},
  {path: 'inventory', canActivate: mapToCanActivate([AuthGuard]), component: InventoryComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: false})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
