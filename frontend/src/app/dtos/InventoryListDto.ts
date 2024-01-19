import {InventoryIngredientDto} from "./InventoryIngredientDto";

export interface InventoryListDto {
  missing: InventoryIngredientDto[]
  available: InventoryIngredientDto[]
}
