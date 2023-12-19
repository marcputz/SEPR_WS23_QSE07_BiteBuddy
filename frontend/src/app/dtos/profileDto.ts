import {AllergeneDto} from "./allergeneDto";
import {IngredientDto} from "./ingredientDto";

/**
 * DTO for Profile related requests to pass data between frontend and backend
 */
export interface ProfileDto {
    id?: number;
    name: string;
    allergens: AllergeneDto[];
    ingredient: IngredientDto[];
}
