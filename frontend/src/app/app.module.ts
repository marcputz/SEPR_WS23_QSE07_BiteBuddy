import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HeaderComponent} from './components/header/header.component';
import {FooterComponent} from './components/footer/footer.component';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/authentication/login/login.component';
import {MessageComponent} from './components/message/message.component';
import {RecipeListComponent} from './components/recipe-list/recipe-list.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {httpInterceptorProviders} from './interceptors';
import { UserSettingsComponent } from './components/user-settings/user-settings.component';
import { RegisterComponent } from './components/authentication/register/register.component';
import { RequestPasswordResetComponent } from './components/authentication/request-password-reset/request-password-reset.component';
import { PasswordResetComponent } from './components/authentication/password-reset/password-reset.component';
import { RecipeDetailComponent } from './components/recipe-detail/recipe-detail.component';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { LandingLayoutComponent } from './layouts/landing-layout/landing-layout.component';
import { LandingHeaderComponent } from './components/landing-header/landing-header.component';
import { LandingFooterComponent } from './components/landing-footer/landing-footer.component';
import {NgOptimizedImage} from "@angular/common";
import { RecipeCreateComponent } from './components/recipe-create/recipe-create.component';
import { AutocompleteComponent } from './components/autocomplete/autocomplete.component';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    LoginComponent,
    MessageComponent,
    RegisterComponent,
    UserSettingsComponent,
    RequestPasswordResetComponent,
    PasswordResetComponent,
    RecipeListComponent,
    RecipeDetailComponent,
    LandingPageComponent,
    LandingLayoutComponent,
    LandingHeaderComponent,
    LandingFooterComponent,
    RecipeCreateComponent,
    AutocompleteComponent,
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        ReactiveFormsModule,
        HttpClientModule,
        NgbModule,
        FormsModule,
        NgOptimizedImage,
    ],
  providers: [httpInterceptorProviders],
  bootstrap: [AppComponent]
})
export class AppModule {
}
