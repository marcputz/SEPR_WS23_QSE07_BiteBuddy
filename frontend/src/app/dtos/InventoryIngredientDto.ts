export interface InventoryIngredientDto {
  name?: string,
  id?: number,
  detailedNamed?: string,
  menuPlanId?: number,
  amount?: number,
  unit?: FoodUnit,
  inventoryStatus: boolean
}

export enum FoodUnit {
  cup,
  tablespoon,
  teaspoon,
  ounce,
  clove,
  pound,
  bunch,
  pinch,
  slices,
  null
}
