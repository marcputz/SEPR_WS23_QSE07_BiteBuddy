import { Injectable } from '@angular/core';
import {Globals} from "../global/globals";
import {HttpClient} from "@angular/common/http";
import {InventoryIngredientDto} from "../dtos/InventoryIngredientDto";

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private baseUri: string = this.globals.backendUri + "/inventory"

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  createInventory() {
    return this.httpClient.get(this.baseUri + "/create");
  }

  getInventory() {
    return this.httpClient.get(this.baseUri + "/")
  }

  updateInventoryIngredient(ingredient: InventoryIngredientDto) {
    return this.httpClient.put(this.baseUri + "/update", ingredient);
  }
}
