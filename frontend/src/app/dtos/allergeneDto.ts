import {AllergeneIngredientDto} from "./allergeneIngredientDto";

/**
 * DTO for Allergene related requests to pass data between frontend and backend
 */
export interface AllergeneDto {
    id: number;
    name: string;
    allergeneIngredients: AllergeneIngredientDto[];
}
