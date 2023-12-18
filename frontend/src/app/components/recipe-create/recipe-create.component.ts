import {Component} from '@angular/core';
import {RecipeService} from "../../services/recipe.service";
import {RecipeDetailsDto, RecipeDto} from "../../dtos/recipe";
import {Observable, of} from "rxjs";
import {values} from "lodash";
import {read} from "@popperjs/core";

@Component({
    selector: 'app-recipe-create',
    templateUrl: './recipe-create.component.html',
    styleUrls: ['./recipe-create.component.scss']
})
export class RecipeCreateComponent {
    ingredient: string = '';
    recipe: RecipeDetailsDto = {
        name: '',
        picture: null,
        description: '',
        id: null,
        ingredients: [],
        allergens: []
    }
    pictureSelected: File = null;

    constructor(
        private service: RecipeService,
        // TODO include Toastr
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

            this.recipe.picture = byteArray;
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
        if (form.valid) {
            // formatting image
            console.log(this.pictureSelected);

            console.log(this.recipe);
            this.service.createRecipe(this.recipe).subscribe({
                    next: data => {
                      // TODO send a success message via toastr
                    }
                }
            );
        }
    }

    formatIngredient(ingredient: String | null) {
        return ingredient ?? '';
    }

    addIngredient(ingredientInput) {
        let ingredient = ingredientInput.value;
        if (ingredient !== null && typeof ingredient === 'string' && ingredient.trim() !== '') {
            this.recipe.ingredients.push(ingredient);
            console.log('Valid input, added: ', ingredient);
        } else {
            console.log("Ingredient not valid!", ingredient?.value);
        }
    }

    ingredientSuggestions = (input: string) => (input === '')
        ? of([])
        : this.service.searchRecipeIngredientsMatching(input);
}
