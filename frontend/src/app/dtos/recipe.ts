export interface RecipeSearch {
  creator?: string;
  name?: string;
}

export interface RecipeIngredientDto {

}

export interface RecipeDto {
  id?: number;
  creationDate?: Date;
  creator?: string;
  name: string;
  instructions?: string;
}

export interface RecipeListDto {
  creator: string;
  name: string;
  id: number;
}

export interface RecipeDetailsDto {
  name: string;
  description: string;
  id: number;
}
