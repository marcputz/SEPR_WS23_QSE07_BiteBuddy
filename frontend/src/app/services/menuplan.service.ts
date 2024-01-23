import {Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Globals} from "../global/globals";
import {MenuPlanCreateDto} from "../dtos/menuplan/menuPlanCreateDto";
import {catchError, Observable, throwError} from "rxjs";
import {MenuPlanDetailDto} from "../dtos/menuplan/menuPlanDetailDto";
import {tap} from "rxjs/operators";
import {MenuPlanListDto} from "../dtos/menuplan/menuPlanListDto";
import {Logger} from "jasmine-spec-reporter/built/display/logger";
import {formatDate} from "@angular/common";
import {InventoryIngredientDto} from "../dtos/InventoryIngredientDto";

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
    return this.httpClient.get(this.baseUri + "/inventory/create");
  }

  getInventory() {
    return this.httpClient.get(this.baseUri + "/inventory/")
  }

  updateInventoryIngredient(ingredient: InventoryIngredientDto) {
    return this.httpClient.put(this.baseUri + "/inventory/update", ingredient);
  }

}
