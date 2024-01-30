import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Globals} from "../global/globals";
import {Observable} from "rxjs";
import {PictureDto} from "../dtos/pictureDto";

@Injectable({
  providedIn: 'root'
})
export class PictureService {

  private baseUri: string = this.globals.backendUri + '/picture';

  private pictureCache: Map<number, PictureDto> = new Map<number, PictureDto>();

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  getPicture(id: number): Observable<PictureDto> {
    if (id == undefined) {
      return null;
    }

    if (this.pictureCache.has(id)) {
      return new Observable(observer => {
        observer.next(this.pictureCache.get(id));
        observer.complete();
      });

    } else {
      let observable: Observable<PictureDto> = this.httpClient.get<PictureDto>(`${this.baseUri}?id=` + id);

      observable.subscribe({
        next: data => {
          this.pictureCache.set(id, data);
        }
      })

      return observable;
    }
  }

  uploadPicture(data: number[]): Observable<PictureDto> {
    if (data == undefined) {
      return null;
    }

    let dto: PictureDto = {
      id: -1,
      data: data,
      description: null
    };

    return this.httpClient.put<PictureDto>(`${this.baseUri}`, {params: {body: {dto}}});
  }
}
