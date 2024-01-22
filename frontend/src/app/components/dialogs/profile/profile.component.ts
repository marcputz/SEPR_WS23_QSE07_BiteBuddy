import { Component } from '@angular/core';
import {ToastrService} from "ngx-toastr";
import {ActivatedRoute, Router} from "@angular/router";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ProfileDto} from "../../../dtos/profileDto";
import {ProfileService} from "../../../services/profile.service";
import {AllergeneDto} from "../../../dtos/allergeneDto";
import {IngredientDto} from "../../../dtos/ingredientDto";
import {IngredientService} from "../../../services/ingredient.service";
import {AllergensService} from "../../../services/allergens.service";
import {AuthService} from "../../../services/auth.service";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent {

  title: string = "Set up your Profile"
  subtitle1: string = "Before we start we need a few informations about you."

  submitted = false;
  isInputFocused: {[key: string]: boolean } = {};

  profile: ProfileDto = {} as ProfileDto;
  allergens: AllergeneDto[] = [];
  ingredient: IngredientDto[] = [];

  form: FormGroup;
  constructor(
      private fb: FormBuilder,
      private service: ProfileService,
      private allergensService: AllergensService,
      private ingredientService: IngredientService,
      private router: Router,
      private route: ActivatedRoute,
      private notification: ToastrService
  ) {
      // Initialize the form in the constructor
      this.form = this.fb.group({
          name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
          allergens: [[], Validators.required],
          ingredient: [[], Validators.required],
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
        this.submitted = true;
        if (this.form.valid) {
            this.profile = this.form.value;
            this.service.create(this.profile)
                .subscribe({
                next: data => {
                    this.notification.success(`Profile ${this.profile?.name} successfully.`);
                    this.router.navigate(['/dashboard']);
                },
                error: error => {
                    console.error('Error creating profile', error);
                    const errorMessage = error?.message || 'Unknown error occured';
                    this.notification.error(`Error creating profile: ${errorMessage}`);
                }
            });
        }
    }

  /**
   * Update the input focus flag in order to show/hide the label on the input field
   */
  updateInputFocus(attribute: string) {
    this.isInputFocused[attribute] = this.form.get(attribute).value !== '';
  }

}
