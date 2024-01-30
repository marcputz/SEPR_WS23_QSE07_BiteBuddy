import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {RecipeDetailsDto, RecipeSearch, RecipeSearchResultDto} from "../dtos/recipe";
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

  public search(searchParams: RecipeSearch): Observable<RecipeSearchResultDto> {
    return this.http.post<RecipeSearchResultDto>(this.baseUri, searchParams);
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
