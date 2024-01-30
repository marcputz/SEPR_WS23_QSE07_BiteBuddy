import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {
  ProfileDetailDto,
  ProfileDto,
  RecipeRatingDto,
  ProfileSearchResultDto,
  ProfileSearch,
  ProfileListDto
} from "../dtos/profileDto";
import {Observable} from "rxjs";
import {ErrorFormatterService} from './error-formatter.service';
import {Globals} from "../global/globals";
import {RecipeRatingListsDto} from "../dtos/recipe";

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  baseUri = this.globals.backendUri + '/profiles';

  constructor(
    private globals: Globals,
    private http: HttpClient
  ) {
  }

  /**
   * Create a new profile in the system.
   *
   * @param profile the data for the profile that should be created
   * @return Observable for the created profile
   */
  create(profile: ProfileDto | undefined): Observable<ProfileDto> {
    console.trace('Creating profile: ' + profile.name);

    return this.http.post<ProfileDto>(
      this.baseUri,
      profile
    );
  }

  public search(searchParams: ProfileSearch): Observable<ProfileSearchResultDto> {
    return this.http.post<ProfileSearchResultDto>(`${this.baseUri}/search`, searchParams);
  }

  getAllProfilesOfUser(): Observable<ProfileListDto[]> {
    return this.http.get<ProfileListDto[]>(this.baseUri);
  }

  createRating(recipeRatingDto: RecipeRatingDto) {
    return this.http.put(`${this.baseUri}/rating/${recipeRatingDto.recipeId}`, recipeRatingDto);
  }

  getRatingLists(userId: number): Observable<RecipeRatingListsDto> {
    return this.http.get<RecipeRatingListsDto>(`${this.baseUri}/rating/${userId}`);
  }

  getProfileDetails(profileId: number): Observable<ProfileDetailDto> {
    return this.http.get<ProfileDetailDto>(`${this.baseUri}/${profileId}`)
  }

  editProfile(profileDto: ProfileDto): Observable<ProfileDto> {
    return this.http.put<ProfileDto>(`${this.baseUri}/edit/${profileDto.id}`, profileDto)
  }

  copyToOwn(profileId: number): Observable<ProfileDetailDto> {
    return this.http.post<ProfileDetailDto>(`${this.baseUri}/copyToOwn/${profileId}`, null)
  }

  deleteProfile(profileId: number): Observable<ProfileDto> {
    return this.http.delete<ProfileDto>(`${this.baseUri}/deleteProfile/${profileId}`);
  }

  setActiveProfile(profileId: number) {
    return this.http.post(`${this.baseUri}/setActive/${profileId}`, null);
  }
}
