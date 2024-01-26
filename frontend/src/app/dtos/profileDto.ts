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

export interface ProfileSearch {
    name: string;
    creator: string;
    ownProfiles: boolean;
    page: number;
    entriesPerPage: number;
}

export interface ProfileSearchResultDto {
    page: number,
    entriesPerPage: number,
    numberOfPages: number,
    profiles: ProfileDetailDto[];
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

export interface ProfileDetailDto {
    id: number,
    name: string,
    allergens: string[],
    ingredients: string[],
    liked: RecipeProfileViewDto[],
    disliked: RecipeProfileViewDto[],
    user: string,
    userId: number
}

export interface ProfileEditDto {
  id: number;
  name: string;
  allergens: AllergeneDto[];
  ingredient: IngredientDto[];
  userId: number;
}
