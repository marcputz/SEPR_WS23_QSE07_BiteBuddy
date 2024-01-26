import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Globals} from "../global/globals";
import {MenuPlanCreateDto} from "../dtos/menuplan/menuPlanCreateDto";
import {Observable} from "rxjs";
import {MenuPlanDetailDto} from "../dtos/menuplan/menuPlanDetailDto";
import {formatDate} from "@angular/common";
import {InventoryIngredientDto} from "../dtos/InventoryIngredientDto";
import {MenuPlanUpdateRecipeDto} from "../dtos/menuplan/menuPlanUpdateRecipeDto";

@Injectable({
  providedIn: 'root'
})
export class MenuPlanService {

  private baseUri: string = this.globals.backendUri + '/menuplan';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  generateMenuPlan(createDto: MenuPlanCreateDto): Observable<MenuPlanDetailDto> {
    return this.httpClient.post<MenuPlanDetailDto>(this.baseUri + "/generate", createDto);
  }
  getMenuPlanForDay(date: string): Observable<MenuPlanDetailDto> {
    const formattedDate = formatDate(date, 'yyyy-MM-dd', 'en-US');
    return this.httpClient.get<MenuPlanDetailDto>(`${this.baseUri}/forDate`, { params: { date: formattedDate } });
  }

  getMenuPlans(): Observable<MenuPlanDetailDto[]>{
    return this.httpClient.get<MenuPlanDetailDto[]>(this.baseUri);
  }

  createInventory() {
    return this.httpClient.get<InventoryIngredientDto>(this.baseUri + "/inventory/create/");
  }

  getInventory() {
    return this.httpClient.get(this.baseUri + "/inventory/")
  }

  updateInventoryIngredient(ingredient: InventoryIngredientDto) {
    return this.httpClient.put(this.baseUri + "/inventory/update", ingredient);
  }
  updateRecepyInMenuPlan(menuPlan: MenuPlanUpdateRecipeDto){
    return this.httpClient.put(this.baseUri + "/update", menuPlan);
  }

}
