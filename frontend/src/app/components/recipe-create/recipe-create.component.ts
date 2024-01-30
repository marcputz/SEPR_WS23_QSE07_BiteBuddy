import {Component, OnInit} from '@angular/core';
import {RecipeService} from "../../services/recipe.service";
import {RecipeDetailsDto, RecipeIngredientDto} from "../../dtos/recipe";
import {DomSanitizer} from "@angular/platform-browser";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {IngredientService} from "../../services/ingredient.service";
import {UserService} from "../../services/user.service";
import {ErrorHandler} from "../../services/errorHandler";
import {ImageHandler} from "../../utils/imageHandler";
import {of} from "rxjs";

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
    picture: [],
  }

  safePictureUrl = null;
  submitButtonClicked = false;
  validImage = true;
  validTitle = true;
  validDescription = true;
  validIngredients = true;

  constructor(
    private service: RecipeService,
    private ingredientService: IngredientService,
    private sanitizer: DomSanitizer,
    private router: Router,
    private notification: ToastrService,
    private errorHandler: ErrorHandler,
    private userService: UserService,
    private imageHandler: ImageHandler
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

  public loadPreviewPicture() {
    if (this.recipe.picture === null) {
      this.safePictureUrl = this.imageHandler.sanitizeRecipeImage(this.recipe.picture);
    } else {
      this.safePictureUrl = this.imageHandler.sanitizeRecipeImage(btoa(
        String.fromCharCode.apply(null, new Uint8Array(this.recipe.picture))));
    }

    return this.safePictureUrl;
  }

  onPictureChange(event) {
    if (event.target.files.length > 0) {
      this.safePictureUrl = event.target.files[0];

      this.imageHandler.prepareRecipePicture(event.target.files[0])
        .then((imageBytes: number[]) => {
          this.recipe.picture = imageBytes;
          this.loadPreviewPicture();
          this.validImage = true;
        })
        .catch(error => {
          console.error('Error processing image: ', error);
          this.notification.error('Unsupported Format, please use small jpg files');
        });
    }
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
          this.submitButtonClicked = false;
        }
      });
    }

    if (this.submitButtonClicked) {
      // validations
      if (this.recipe.picture === null || this.recipe.picture.length == 0) {
        this.validImage = false;
      }

      if (this.recipe.name.trim().length == 0) {
        this.validTitle = false;
      }

      if (this.recipe.description.trim().length == 0) {
        this.validDescription = false;
      }

      if (this.recipe.ingredients !== null) {
        if (this.recipe.ingredients.length == 0) {
          this.validIngredients = false;
        } else {
          for (let ing of this.recipe.ingredients) {
            if (ing.amount <= 0) {
              this.validIngredients = false;
            }
          }
        }
      }
      this.submitButtonClicked = false;
    }
  }

  onButtonClick() {
    this.submitButtonClicked = true;
  }

  formatIngredient(ingredient: String | null) {
    return ingredient ?? '';
  }

  addIngredient(ingredientInput) {
    this.validIngredients = true;
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

  removeIngredient(ingredient) {
    const indexToRemove = this.recipe.ingredients.indexOf(ingredient);
    this.recipe.ingredients.splice(indexToRemove, 1);
  }

  ingredientSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ingredientService.searchRecipeIngredientsMatching(input);
}
