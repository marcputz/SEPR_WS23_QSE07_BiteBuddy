import {Component, OnInit} from '@angular/core';
import {RecipeService} from "../../services/recipe.service";
import {RecipeListDto, RecipeSearch, RecipeSearchResultDto} from "../../dtos/recipe";
import {debounceTime, Subject} from "rxjs";
import {DomSanitizer, SafeUrl} from '@angular/platform-browser';
import {ToastrService} from "ngx-toastr";
import {RecipeRatingDto} from "../../dtos/profileDto";
import {UserService} from "../../services/user.service";
import {UserSettingsDto} from "../../dtos/userSettingsDto";
import {ProfileService} from "../../services/profile.service";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {Router} from "@angular/router";
import {PictureService} from "../../services/picture.service";
import {PictureDto} from "../../dtos/pictureDto";
import {ErrorHandler} from "../../services/errorHandler";

@Component({
  selector: 'app-recipe-list',
  templateUrl: './recipe-list.component.html',
  styleUrls: ['./recipe-list.component.scss']
})
export class RecipeListComponent implements OnInit {
  recipes: RecipeListDto[] = [];
  recipeImages: Map<RecipeListDto, number[]> = null;
  recipeImageAlts: Map<RecipeListDto, string> = null;
  maxPages: number = 5;
  pagesForPagination: number[];
  searchChangedObservable = new Subject<void>();
  searchParams: RecipeSearch = {
    creator: "",
    name: "",
    page: 0,
    entriesPerPage: 30,
  };

  searchResponse: RecipeSearchResultDto;

  recipeRating: RecipeRatingDto = {
    recipeId: -100,
    userId: -100,
    rating: -1
  };

  likes: number[] = [];
  dislikes: number[] = [];

  constructor(
    private service: RecipeService,
    private authService: UserService,
    private profileService: ProfileService,
    private pictureService: PictureService,
    private sanitizer: DomSanitizer,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private router: Router,
    private errorHandler: ErrorHandler
  ) {
  }

  ngOnInit() {
    this.reloadRecipes();

    this.searchChangedObservable
      .pipe(debounceTime(300))
      .subscribe({next: () => this.reloadRecipes()});

    this.authService.getUser().subscribe(
      (settings: UserSettingsDto) => {
        this.profileService.getRatingLists(settings.id)
          .subscribe({
            next: data => {
              this.likes = data.likes;
              this.dislikes = data.dislikes

              this.createPagination();
            },
            error: error => {

              let errorObj = this.errorHandler.getErrorObject(error);

              switch (errorObj.status) {
                case 404:
                  this.notification.warning("You need to create a profile before using the Website");
                  this.router.navigate(["/profile"])
                  break;
                default:
                  this.errorHandler.handleApiError(errorObj);
                  break;
              }

            }
          });
      },
      error => {
        console.error('Error getting user settings', error);
        const errorMessage = error?.error || 'Unknown error occurred';

        let errorObj = this.errorHandler.getErrorObject(error);

        if (error.status === 401) {
          // Handle logout logic, e.g., redirect to login page
          this.errorHandler.handleApiError(errorObj);
        }
      }
    );

  }

  searchChanged(): void {
    this.searchChangedObservable.next();
  }

  reloadRecipes() {
    this.service.search(this.searchParams).subscribe({
      next: data => {
        this.searchResponse = data;
        this.recipes = data.recipes;
        this.maxPages = data.numberOfPages;
        this.searchParams.page = data.page;

        this.createPagination()

        // load recipe images
        this.recipeImages = new Map<RecipeListDto, number[]>();
        this.recipeImageAlts = new Map<RecipeListDto, string>();
        for (let dto of this.recipes) {
          this.pictureService.getPicture(dto.pictureId).subscribe({
            next: (pictureDto) => {
              this.recipeImages.set(dto, pictureDto.data);
              this.recipeImageAlts.set(dto, pictureDto.description);
            },
            error: error => {
              let errorObj = this.errorHandler.getErrorObject(error);
              this.errorHandler.handleApiError(errorObj);
            }
          });
        }
      },
      error: err => {
        // this.notification.error('Error fetching recipes', err)
        let errorObj = this.errorHandler.getErrorObject(err);
        this.errorHandler.handleApiError(errorObj);
      }
    })
  }

  getImageFor(recipe: RecipeListDto) {
    if (this.recipeImages.has(recipe)) {
      return this.sanitizeImage(this.recipeImages.get(recipe));
    } else {
      return this.sanitizer.bypassSecurityTrustUrl("assets/images/recipe_default.png");
    }
  }

  getImageAltFor(recipe: RecipeListDto) {
    if (this.recipeImageAlts.has(recipe)) {
      return this.recipeImageAlts.get(recipe);
    } else {
      return "Recipe Image";
    }
  }

  sanitizeImage(imageBytes: any): SafeUrl {
    if (imageBytes != null) {
      try {
        if (!imageBytes || imageBytes.length === 0) {
          // throw new Error('Empty or undefined imageBytes');
        }

        const base64Image = btoa(String.fromCharCode.apply(null, new Uint8Array(imageBytes)));
        const dataUrl = `data:image/jpg;base64,${imageBytes}`;
        return this.sanitizer.bypassSecurityTrustUrl(dataUrl);
      } catch (error) {
        return this.sanitizer.bypassSecurityTrustUrl(''); // Return a safe, empty URL or handle the error accordingly
      }
    }
    return "";
  }

  pageChanger(newPageNumber: number) {
    this.searchParams.page = newPageNumber;
    this.reloadRecipes()
  }

  rateRecipe(recipeId: number, rating: number) {
    this.recipeRating.recipeId = recipeId;
    this.recipeRating.rating = rating;
    this.authService.getUser().subscribe(
      (settings: UserSettingsDto) => {
        this.recipeRating.userId = settings.id;
        this.profileService.createRating(this.recipeRating)
          .subscribe({
              next: data => {
                this.notification.success("Successfully rated new recipe!");
                this.authService.getUser().subscribe(
                  (settings: UserSettingsDto) => {
                    this.profileService.getRatingLists(settings.id)
                      .subscribe({
                        next: data => {
                          this.likes = data.likes;
                          this.dislikes = data.dislikes
                        },
                        error: error => {

                          let errorObj = this.errorHandler.getErrorObject(error);
                          this.errorHandler.handleApiError(errorObj);

                        }
                      });
                  },
                );
              },
              error: error => {

                let errorObj = this.errorHandler.getErrorObject(error);

                switch (errorObj.status) {
                  case 500:
                    this.notification.warning("Can not rate recipe, please try again later");
                    break;
                  default:
                    this.errorHandler.handleApiError(errorObj);
                    break;
                }
              }
            }
          );
      });
  }

  redirectToRecipe(recipeId: number) {
    this.router.navigate(['/recipes', recipeId]);
  }

  createPagination() {
    function arange(size: number, start: number = 0): number[] {
      return Array.from({length: size}, (_, index) => start + index);
    }

    // when having less than 5 pages we only show available pages
    if (this.maxPages < 5) {
      this.pagesForPagination = arange(this.maxPages, 0);
    } else if (this.searchParams.page >= (this.maxPages - 3)) {
      this.pagesForPagination = arange(5, (this.maxPages - 5));
    } else {
      this.pagesForPagination = arange(5, Math.max(this.searchParams.page - 2, 0));
    }
  }
}
