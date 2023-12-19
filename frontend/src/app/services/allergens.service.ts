import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {ErrorFormatterService} from "./error-formatter.service";
import {AllergeneDto} from "../dtos/allergeneDto";
import {catchError, Observable, throwError} from "rxjs";
import {Globals} from '../global/globals';

@Injectable({
    providedIn: 'root'
})
export class AllergensService {
    httpHeaders = new HttpHeaders({ 'Content-Type': 'application/json' });

    baseUri = this.globals.backendUri + '/allergens';

    constructor(
        private globals: Globals,
        private http: HttpClient,
        private errorFormatterService: ErrorFormatterService
    ) { }

    /**
     * Get all allergens from the backend
     *
     * @return Observable for the list of allergens
     */
    getAllAllergens(): Observable<AllergeneDto[]> {
        return this.http.get<AllergeneDto[]>(`${this.baseUri}`)
            .pipe(
                catchError(error => {
                    const formattedError = this.errorFormatterService.format(error);
                    console.error('Error occurred:', formattedError); // Optional: log the formatted error
                    return throwError(() => new Error(formattedError)) // Re-throw the error so you can handle it in your components
                })
            );
    }
}
