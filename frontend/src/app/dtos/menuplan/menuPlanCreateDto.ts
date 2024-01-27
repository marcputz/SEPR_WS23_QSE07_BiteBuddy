import {InventoryIngredientDto} from "../InventoryIngredientDto";

export interface MenuPlanCreateDto {
  profileId: number;
  fromTime: string;
  untilTime: string;
  fridge: string[]
}
