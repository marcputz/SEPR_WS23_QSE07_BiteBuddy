import {AllergeneDto} from "./allergeneDto";
import {IngredientDto} from "./ingredientDto";
import {RecipeProfileViewDto} from "./recipe";

/**
 * DTO for Profile related requests to pass data between frontend and backend
 */
export interface ProfileDto {
    id?: number;
    name: string;
    allergens: AllergeneDto[];
    ingredient: IngredientDto[];
    userId: number;
}

export interface RecipeRatingDto {
  recipeId: number;
  userId: number;
  rating: number;
}

export interface CheckRatingDto {
  recipeId: number;
  userId: number;
}

export interface ProfileDetailDto{
  id: number,
  name: string,
  allergens: string[],
  ingredients: string[],
  liked: RecipeProfileViewDto[],
  disliked: RecipeProfileViewDto[],
  user: string
}
