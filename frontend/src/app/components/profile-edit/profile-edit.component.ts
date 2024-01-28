import {Component, numberAttribute} from '@angular/core';
import {ToastrService} from "ngx-toastr";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ProfileDetailDto, ProfileDto, ProfileEditDto} from "../../dtos/profileDto";
import {ProfileService} from "../../services/profile.service";
import {AllergeneDto} from "../../dtos/allergeneDto";
import {IngredientDto} from "../../dtos/ingredientDto";
import {IngredientService} from "../../services/ingredient.service";
import {AllergensService} from "../../services/allergens.service";
import {AuthService} from "../../services/auth.service";
import {UserSettingsDto} from "../../dtos/userSettingsDto";
import {Observable} from "rxjs";
import {RecipeDetailsDto} from "../../dtos/recipe";

@Component({
  selector: 'app-profile-edit',
  templateUrl: './profile-edit.component.html',
  styleUrls: ['./profile-edit.component.scss']
})
export class ProfileEditComponent {

  title: string = "Edit Profile"

  isInputFocused: {[key: string]: boolean } = {};
  submitted = false;

  profile: ProfileDto = {} as ProfileDto;
  previousProfileDetails: ProfileDetailDto = {} as ProfileDetailDto;
  allergens: AllergeneDto[] = [];
  ingredient: IngredientDto[] = [];

  gluten: string = "Eggs";

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
    private authService: AuthService
  ) {
    // Initialize the form in the constructor
    this.form = this.fb.group({
      name: ['', Validators.required],
      allergens: [[], Validators.required],
      ingredient: [[], Validators.required],
    });
  }

  ngOnInit(): void {
    const routeParams: ParamMap = this.route.snapshot.paramMap;
    this.service.getProfileDetails(Number(routeParams.get('id'))).subscribe({
      next: profileDetails => {
        this.previousProfileDetails = profileDetails;
        console.log(this.previousProfileDetails)
      },
      error: error => {
        console.error('Error retrieving profile information', error);
        const errorMessage = error?.message || 'Unknown error occured';
        this.notification.error(`Error retrieving profile information: ${errorMessage}`);
      }
    }
    );
    //get all the list of allergens
    this.allergensService.getAllAllergens().subscribe({
      next: allergens => {
        this.allergens = allergens;
        console.log(this.allergens)
      },
      error: error => {
        console.error('Error retrieving all allergens', error);
        const errorMessage = error?.message || 'Unknown error occured';
        this.notification.error(`Error retrieving all allergens: ${errorMessage}`);
      }
    });

    //get all the list of allergens
    this.ingredientService.getAllIngredients().subscribe({
      next: ingredient => {
        this.ingredient = ingredient;
        console.log(this.ingredient)
      },
      error: error => {
        console.error('Error retrieving all ingredient', error);
        const errorMessage = error?.message || 'Unknown error occured';
        this.notification.error(`Error retrieving all ingredient: ${errorMessage}`);
      }
    });
  }

  public onSubmit(): void {
    console.log('is form valid?', this.form.valid, this.form.value);

    if (this.form.valid) {
      this.profile = this.form.value;
      this.authService.getUser().subscribe(
        (settings: UserSettingsDto) => {
          this.user = settings;
          this.profile.id = this.previousProfileDetails.id;
          this.profile.userId = this.previousProfileDetails.userId;
          console.log(this.profile)
          this.service.editProfile(this.profile)
            .subscribe({
              next: data => {
                this.notification.success(`Profile ${this.profile?.name} successfully edited.`);
                this.router.navigate(['/profiles']);
              },
              error: error => {
                console.error('Error editing profile', error);
                const errorMessage = error?.message || 'Unknown error occured';
                this.notification.error(`Error creating profile: ${errorMessage}`);
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
