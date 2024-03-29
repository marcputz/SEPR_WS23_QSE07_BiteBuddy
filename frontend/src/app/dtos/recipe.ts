import {PictureDto} from "./pictureDto";

export interface RecipeSearch {
  creator?: string;
  name?: string;
  page: number;
  entriesPerPage: number;
}

export interface RecipeIngredientDto {
  name: string,
  amount: number,
  unit: string;
}

export interface RecipeDto {
  id?: number;
  creationDate?: Date;
  creator?: string;
  name: string;
  instructions?: string;
  pictureId: number;
}

export interface RecipeListDto {
  creator: string;
  name: string;
  id: number;
  pictureId: number;
}

export interface RecipeDetailsDto {
  name: string;
  creatorName: string;
  description: string;
  id: number;
  ingredients: RecipeIngredientDto[];
  allergens: string[];
  picture?: number[];
  pictureId?: number;
}

export interface RecipeDetailsViewDto {
  name: string;
  description: string;
  id: number;
  ingredients: RecipeIngredientDto[];
  allergens: string[];
  pictureId: number;
  rating: number;
}

export interface RecipeSearchResultDto {
  page: number,
  entriesPerPage: number,
  numberOfPages: number,
  recipes: RecipeListDto[];
}

export interface RecipeRatingListsDto {
  likes: number[];
  dislikes: number[];
}

export interface RecipeDetailsViewDto {
  name: string;
  description: string;
  id: number;
  ingredients: RecipeIngredientDto[];
  allergens: string[];
  picture: number[];
  rating: number;
}

export interface RecipeSearchResultDto {
  page: number,
  entriesPerPage: number,
  numberOfPages: number,
  recipes: RecipeListDto[];
}

export interface RecipeRatingListsDto {
  likes: number[];
  dislikes: number[];
}

export interface RecipeProfileViewDto {
  id: number,
  name: string
}
