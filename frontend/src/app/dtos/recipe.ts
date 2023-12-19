export interface RecipeSearch {
  creator?: string;
  name?: string;
  page: number;
  entriesPerPage: number;
}

export interface RecipeIngredientDto {

}

export interface RecipeDto {
  id?: number;
  creationDate?: Date;
  creator?: string;
  name: string;
  instructions?: string;
  picture: number[];
}

export interface RecipeListDto {
  creator: string;
  name: string;
  id: number;
  picture: number[];
}

export interface RecipeDetailsDto {
  name: string;
  description: string;
  id: number;
  ingredients: string[];
  allergens: string[];
  picture: number[];
}
