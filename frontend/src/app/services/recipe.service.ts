import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {RecipeDetailsDto, RecipeDto, RecipeListDto, RecipeSearch} from "../dtos/recipe";
import {Observable} from "rxjs";
import {Globals} from '../global/globals';


@Injectable({
  providedIn: 'root'
})
export class RecipeService {
  baseUri = this.globals.backendUri + "/recipes"

  constructor(
    private http: HttpClient,
    private globals: Globals
  ) {
  }

  public search(searchParams: RecipeSearch): Observable<RecipeListDto[]> {
    return this.http.post<RecipeListDto[]>(this.baseUri, searchParams);
  }

  getById(id: number): Observable<RecipeDetailsDto> {
    return this.http.get<RecipeDetailsDto>(`${this.baseUri}/${id}`);
  }

  createRecipe(recipe: RecipeDetailsDto) {
    return this.http.post(this.baseUri + "/create", recipe);
  }

  searchRecipeIngredientsMatching(term: string) {
    return this.http.get(`${this.baseUri}/ingredient/${term}`);
  }
}
