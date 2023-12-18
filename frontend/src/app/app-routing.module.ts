import {NgModule} from '@angular/core';
import {mapToCanActivate, RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/authentication/login/login.component';
import {AuthGuard} from './guards/auth.guard';
import {MessageComponent} from './components/message/message.component';
import {UserSettingsComponent} from './components/user-settings/user-settings.component';
import {RegisterComponent} from "./components/authentication/register/register.component";
import {
  RequestPasswordResetComponent
} from "./components/authentication/request-password-reset/request-password-reset.component";
import {PasswordResetComponent} from "./components/authentication/password-reset/password-reset.component";
import {RecipeListComponent} from "./components/recipe-list/recipe-list.component";
import {RecipeDetailComponent} from "./components/recipe-detail/recipe-detail.component";
import {LandingLayoutComponent} from "./layouts/landing-layout/landing-layout.component";
import {RecipeCreateComponent} from "./components/recipe-create/recipe-create.component";

const routes: Routes = [
  //{path: '', component: HomeComponent},
  {path: '', component: LandingLayoutComponent},
  {path: 'login', component: LoginComponent},
  {path: 'request_password_reset', component: RequestPasswordResetComponent},
  {path: 'password_reset', component: PasswordResetComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'settings', canActivate: mapToCanActivate([AuthGuard]), component: UserSettingsComponent},
  {path: 'message', canActivate: mapToCanActivate([AuthGuard]), component: MessageComponent},
  {path: 'recipes', children: [
      {path: '', component: RecipeListComponent},
      {path: 'create', component: RecipeCreateComponent},
      {path: ':id', component: RecipeDetailComponent},
  ]},
  {path: '*', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: false})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
