import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {RecipeDto, RecipeListDto, RecipeSearch} from "../dtos/recipe";
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
    let params = new HttpParams();
    if (searchParams.name) {
      params = params.append('name', searchParams.name);
    }

    if (searchParams.maxCount) {
      params = params.append('maxCount', searchParams.maxCount);
    } else {
      params = params.append('maxCount', -1);
    }

    if (searchParams.creator) {
      params = params.append('creator', searchParams.creator);
    }

    return this.http.get<RecipeListDto[]>(this.baseUri, { params });
  }
}
