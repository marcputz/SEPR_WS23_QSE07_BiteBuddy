import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {LoginComponent} from './components/dialogs/login/login.component';
import {RecipeListComponent} from './components/recipe-list/recipe-list.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {httpInterceptorProviders} from './interceptors';
import {RegisterComponent} from './components/dialogs/register/register.component';
import { RequestPasswordResetComponent } from './components/dialogs/request-password-reset/request-password-reset.component';
import {PasswordResetComponent} from './components/dialogs/password-reset/password-reset.component';
import {RecipeDetailComponent} from './components/recipe-detail/recipe-detail.component';
import {LandingPageComponent} from './components/landing-page/landing-page/landing-page.component';
import {LandingLayoutComponent} from './layouts/landing-layout/landing-layout.component';
import {LandingHeaderComponent} from './components/landing-page/landing-header/landing-header.component';
import {LandingFooterComponent} from './components/landing-page/landing-footer/landing-footer.component';
import {DialogLayoutComponent} from './layouts/dialog-layout/dialog-layout.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {ToastrModule} from "ngx-toastr";
import {ChangeEmailComponent} from './components/user-settings/change-email/change-email.component';
import {ChangePasswordComponent} from './components/user-settings/change-password/change-password.component';
import {ChangeSettingsComponent} from './components/user-settings/change-settings/change-settings.component';
import { SettingsLayoutComponent } from './layouts/settings-layout/settings-layout.component';
import {DatePipe, NgOptimizedImage} from "@angular/common";
import { RecipeCreateComponent } from './components/recipe-create/recipe-create.component';
import { AutocompleteComponent } from './components/autocomplete/autocomplete.component';
import { ProfileComponent } from './components/dialogs/profile/profile.component';
import { MenuPlanComponent } from './components/menu-plan/menu-plan.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    RequestPasswordResetComponent,
    PasswordResetComponent,
    RecipeListComponent,
    RecipeDetailComponent,
    LandingPageComponent,
    LandingLayoutComponent,
    LandingHeaderComponent,
    LandingFooterComponent,
    DialogLayoutComponent,
    ChangeEmailComponent,
    ChangePasswordComponent,
    ChangeSettingsComponent,
    SettingsLayoutComponent,
    RecipeCreateComponent,
    AutocompleteComponent,
    ProfileComponent,
    MenuPlanComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    NgbModule,
    FormsModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot(),
    NgOptimizedImage,
  ],
  providers: [
    httpInterceptorProviders,
    DatePipe
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
