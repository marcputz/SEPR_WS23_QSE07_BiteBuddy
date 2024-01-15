import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Globals} from "../global/globals";
import {MenuPlanCreateDto} from "../dtos/menuplan/menuPlanCreateDto";
import {catchError, Observable, throwError} from "rxjs";
import {MenuPlanDetailDto} from "../dtos/menuplan/menuPlanDetailDto";
import {tap} from "rxjs/operators";

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

}
