import {Component} from '@angular/core';
import {RecipeService} from "../../services/recipe.service";
import {RecipeDetailsDto, RecipeDto, RecipeIngredientDto} from "../../dtos/recipe";
import {Observable, of} from "rxjs";
import {values} from "lodash";
import {read} from "@popperjs/core";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {PictureService} from "../../services/picture.service";

@Component({
  selector: 'app-recipe-create',
  templateUrl: './recipe-create.component.html',
  styleUrls: ['./recipe-create.component.scss']
})
export class RecipeCreateComponent {
  ingredient: string = '';
  recipe: RecipeDetailsDto = {
    name: '',
    pictureId: null,
    description: '',
    id: null,
    ingredients: [],
    allergens: [],
  }
  recipePicture: number[] = null;
  pictureSelected: File = null;
  submitButtonClicked = false;

  constructor(
    private service: RecipeService,
    private pictureService: PictureService,
    private sanitizer: DomSanitizer,
    private router: Router,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
  ) {
  }

  ngOnInit(): void {

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
      // formatting image
      console.log(this.pictureSelected);

      console.log(this.recipe);

      this.pictureService.uploadPicture(this.recipePicture).subscribe({
        next: data => {

          this.recipe.id = data.id;

          this.service.createRecipe(this.recipe).subscribe({
              next: data => {
                this.notification.success("Successfully created new recipe!")

                this.router.navigate(['/recipes']);
              },
              error: error => {
                console.log(error)
                console.error(error.message, error);
                let title = "Could not create recipe!";
                this.notification.error(this.errorFormatter.format(error), title, {
                  enableHtml: true,
                  timeOut: 5000,
                });
              }
            }
          );

        },
        error: error => {
          console.error(error);
        }
      })
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
      console.log('Valid input, added: ', ingredient);
    } else {
      console.log("Ingredient not valid!", ingredient?.name);
    }
  }

  sanitizeImage(imageBytes: any): SafeUrl {
    // imageBytes = btoa(String.fromCharCode.apply(null, new Uint8Array(imageBytes)));

    imageBytes = String.fromCharCode.apply(null, new Uint8Array(imageBytes));
    imageBytes = btoa(imageBytes);
    console.log(imageBytes);

    try {
      if (!imageBytes || imageBytes.length === 0) {
        throw new Error('Empty or undefined imageBytes');
      }
      const dataUrl = `data:image/png;base64,${imageBytes}`;
      return this.sanitizer.bypassSecurityTrustUrl(dataUrl);
    } catch (error) {
      console.error('Error sanitizing image:', error);
      return this.sanitizer.bypassSecurityTrustUrl(''); // Return a safe, empty URL or handle the error accordingly
    }
  }

  ingredientSuggestions = (input: string) => (input === '')
    ? of([])
    : this.service.searchRecipeIngredientsMatching(input);
}
