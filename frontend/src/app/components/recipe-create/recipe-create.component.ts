import {Component, OnInit} from '@angular/core';
import {RecipeService} from "../../services/recipe.service";
import {RecipeDetailsDto, RecipeDto, RecipeIngredientDto} from "../../dtos/recipe";
import {Observable, of} from "rxjs";
import {values} from "lodash";
import {read} from "@popperjs/core";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {IngredientService} from "../../services/ingredient.service";

import {UserService} from "../../services/user.service";
import {ErrorHandler} from "../../services/errorHandler";

@Component({
  selector: 'app-recipe-create',
  templateUrl: './recipe-create.component.html',
  styleUrls: ['./recipe-create.component.scss']
})
export class RecipeCreateComponent implements OnInit {
  ingredient: string = '';
  recipe: RecipeDetailsDto = {
    name: '',
    creatorName: '',
    description: '',
    id: null,
    ingredients: [],
    allergens: [],
    picture: null,
  }
  recipePicture: number[] = null;
  pictureSelected: File = null;
  submitButtonClicked = false;

  constructor(
    private service: RecipeService,
    private ingredientService: IngredientService,
    private sanitizer: DomSanitizer,
    private router: Router,
    private notification: ToastrService,
    private errorHandler: ErrorHandler,
    private userService: UserService
  ) {
  }

  ngOnInit(): void {
    this.userService.isLoggedInForCreationComponent().subscribe({
      next: value => {
        if (!value) {
          this.notification.error("You need to be logged in to create a recipe");
          this.router.navigate(["/login"]);
        }
      },
      error: err => {
        let errorDto = this.errorHandler.getErrorObject(err);
        this.errorHandler.handleApiError(errorDto);
      }
    })
  }


  onPictureChange(event) {
    this.pictureSelected = event.target.files[0];

    let reader = new FileReader();
    reader.onloadend = () => {
      const base64String = reader.result as string;
      // Convert the base64 string to a Uint8Array
      const arrayBuffer = this.base64ToArrayBuffer(base64String);

      // Convert the ArrayBuffer to an array of numbers (uint8)
      const byteArray = Array.from(new Uint8Array(arrayBuffer));

      this.recipePicture = byteArray;
      this.recipe.picture = this.recipePicture;
    }
    reader.readAsDataURL(this.pictureSelected);
  }

  private base64ToArrayBuffer(base64: string): ArrayBuffer {
    const binaryString = atob(base64.split(',')[1]);
    const length = binaryString.length;
    const bytes = new Uint8Array(length);

    for (let i = 0; i < length; i++) {
      bytes[i] = binaryString.charCodeAt(i);
    }

    return bytes.buffer;
  }

  onSubmit(form): void {
    if (form.valid && this.submitButtonClicked) {
      this.service.createRecipe(this.recipe).subscribe({
        next: data => {
          this.notification.success("Recipe created!");
          this.router.navigate(['/recipes']);
        },
        error: error => {
          let errorObj = this.errorHandler.getErrorObject(error);
          this.errorHandler.handleApiError(errorObj);
        }
      });
    }
  }

  onButtonClick() {
    this.submitButtonClicked = true;
  }

  formatIngredient(ingredient: String | null) {
    return ingredient ?? '';
  }

  addIngredient(ingredientInput) {
    let ingredient: RecipeIngredientDto = {
      name: ingredientInput.value,
      amount: 100,
      unit: "ounce"
    }

    if (ingredient.name.trim() !== '') {
      this.recipe.ingredients.push(ingredient);
    } else {
      console.warn("Invalid Ingredient: ", ingredient?.name);
    }
  }

  sanitizeImage(imageBytes: any): SafeUrl {
    // imageBytes = btoa(String.fromCharCode.apply(null, new Uint8Array(imageBytes)));

    imageBytes = String.fromCharCode.apply(null, new Uint8Array(imageBytes));
    imageBytes = btoa(imageBytes);

    try {
      if (!imageBytes || imageBytes.length === 0) {
        throw new Error('Empty or undefined imageBytes');
      }
      const dataUrl = `data:image/jpeg;base64,${imageBytes}`;
      return this.sanitizer.bypassSecurityTrustUrl(dataUrl);
    } catch (error) {
      console.error('Error sanitizing image:', error);
      return this.sanitizer.bypassSecurityTrustUrl(''); // Return a safe, empty URL or handle the error accordingly
    }
  }

  removeIngredient(ingredient) {
    const indexToRemove = this.recipe.ingredients.indexOf(ingredient);
    this.recipe.ingredients.splice(indexToRemove, 1);
  }

  ingredientSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ingredientService.searchRecipeIngredientsMatching(input);
}
