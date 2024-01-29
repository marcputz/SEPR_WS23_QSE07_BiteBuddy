import {Component} from '@angular/core';
import {ToastrService} from "ngx-toastr";
import {ActivatedRoute, Router} from "@angular/router";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ProfileDto} from "../../../dtos/profileDto";
import {ProfileService} from "../../../services/profile.service";
import {AllergeneDto} from "../../../dtos/allergeneDto";
import {IngredientDto} from "../../../dtos/ingredientDto";
import {IngredientService} from "../../../services/ingredient.service";
import {AllergensService} from "../../../services/allergens.service";
import {UserService} from "../../../services/user.service";
import {UserSettingsDto} from "../../../dtos/userSettingsDto";
import {ErrorHandler} from "../../../services/errorHandler";
import {Observable} from "rxjs";
import {RecipeDetailsDto} from "../../../dtos/recipe";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent {

  title: string = "Set up your Profile"

  submitted = false;
  isInputFocused: {[key: string]: boolean } = {};

  profile: ProfileDto = {} as ProfileDto;
  allergens: AllergeneDto[] = [];
  ingredient: IngredientDto[] = [];

  user: UserSettingsDto = {} as UserSettingsDto;
  userId: number = 1;

  form: FormGroup;
  constructor(
      private fb: FormBuilder,
      private service: ProfileService,
      private allergensService: AllergensService,
      private ingredientService: IngredientService,
      private router: Router,
      private route: ActivatedRoute,
      private notification: ToastrService,
      private authService: UserService,
      private errorHandler: ErrorHandler
  ) {
      // Initialize the form in the constructor
      this.form = this.fb.group({
          name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
          allergens: [[]],
          ingredient: [[]],
      });
  }

  ngOnInit(): void {
      //get all the list of allergens
      this.allergensService.getAllAllergens().subscribe({
          next: allergens => {
            this.allergens = allergens;
            console.log(this.allergens)
            },
            error: error => {
              let errorObj = this.errorHandler.getErrorObject(error);
              this.errorHandler.handleApiError(errorObj);
          }
    });

    //get all the list of allergens
    this.ingredientService.getAllIngredients().subscribe({
       next: ingredient => {
         this.ingredient = ingredient;
         console.log(this.ingredient)
       },
       error: error => {
         let errorObj = this.errorHandler.getErrorObject(error);
         this.errorHandler.handleApiError(errorObj);
       }
    });
  }

  public onSubmit(): void {
        console.log('is form valid?', this.form.valid, this.form.value);
        this.submitted = true;
        if (this.form.valid) {
          this.profile = this.form.value;
          this.authService.getUser().subscribe(
            (settings: UserSettingsDto) => {
              this.user = settings;
              this.profile.userId = settings.id;
              console.log(settings);
              this.service.create(this.profile)
                .subscribe({
                  next: data => {
                    this.notification.success(`Profile ${this.profile?.name} successfully created.`);
                    this.router.navigate(['/recipes']);
                  },
                  error: error => {
                    let errorObj = this.errorHandler.getErrorObject(error);
                    this.errorHandler.handleApiError(errorObj);
                  }
                });
            },
          );

        }
    }

  /**
   * Update the input focus flag in order to show/hide the label on the input field
   */
  updateInputFocus(attribute: string) {
    this.isInputFocused[attribute] = this.form.get(attribute).value !== '';
  }

}
