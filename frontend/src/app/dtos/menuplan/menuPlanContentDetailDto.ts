import {RecipeListDto} from "../recipe";

export interface MenuPlanContentDetailDto {
  day: number;
  timeslot: number;
  recipe: RecipeListDto;
}
