import {Globals} from '../global/globals';
import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {ErrorFormatterService} from "./error-formatter.service";
import {catchError, Observable, throwError} from "rxjs";
import {IngredientDto} from "../dtos/ingredientDto";

@Injectable({
    providedIn: 'root'
})
export class IngredientService {
    httpHeaders = new HttpHeaders({ 'Content-Type': 'application/json' });

    baseUri = this.globals.backendUri + '/ingredients';

    constructor(
        private globals: Globals,
        private http: HttpClient,
        private errorFormatterService: ErrorFormatterService
    ) { }

    getAllIngredients(): Observable<IngredientDto[]> {
        return this.http.get<IngredientDto[]>(`${this.baseUri}`)
            .pipe(
                catchError(error => {
                    const formattedError = this.errorFormatterService.format(error);
                    console.error('Error occurred:', formattedError); // Optional: log the formatted error
                    return throwError(() => new Error(formattedError)) // Re-throw the error so you can handle it in your components
                })
            );
    }

  searchRecipeIngredientsMatching(term: string) {
    return this.http.get(`${this.baseUri}/${term}`);
  }
}
