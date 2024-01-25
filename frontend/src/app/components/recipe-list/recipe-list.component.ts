import {Component} from '@angular/core';
import {RecipeService} from "../../services/recipe.service";
import {RecipeListDto, RecipeSearch, RecipeSearchResultDto} from "../../dtos/recipe";
import {debounceTime, Subject} from "rxjs";
import {DomSanitizer, SafeUrl} from '@angular/platform-browser';
import {ToastrService} from "ngx-toastr";
import {RecipeRatingDto} from "../../dtos/profileDto";
import {AuthService} from "../../services/auth.service";
import {UserSettingsDto} from "../../dtos/userSettingsDto";
import {ProfileService} from "../../services/profile.service";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-recipe-list',
  templateUrl: './recipe-list.component.html',
  styleUrls: ['./recipe-list.component.scss']
})
export class RecipeListComponent {
  recipes: RecipeListDto[] = [];
  maxPages: number = 5;
  searchChangedObservable = new Subject<void>();
  searchParams: RecipeSearch = {
    creator: "",
    name: "",
    page: 0,
    entriesPerPage: 21,
  };

  searchResponse: RecipeSearchResultDto;

  recipeRating: RecipeRatingDto = {
    recipeId: -100,
    userId: -100,
    rating: -1
  };

  likes: number[];
  dislikes: number[];

  constructor(
    private service: RecipeService,
    private authService: AuthService,
    private profileService: ProfileService,
    private sanitizer: DomSanitizer,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private router: Router,
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
                      },
                      error: error => {
                          console.error('Error getting rating list', error);
                          const errorMessage = error?.message || 'Unknown error occured';
                          this.notification.error(`Error getting rating lists: ${errorMessage}`);
                      }
                  });
          },
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
        console.log("number of pages: " + data.numberOfPages);
        console.log("recipes available: " + data.recipes.length);
      },
      error: err => {
        this.notification.error('Error fetching recipes', err)
      }
    })
  }

  sanitizeImage(imageBytes: any): SafeUrl {
    try {
      if (!imageBytes || imageBytes.length === 0) {
        throw new Error('Empty or undefined imageBytes');
      }

      const base64Image = btoa(String.fromCharCode.apply(null, new Uint8Array(imageBytes)));
      const dataUrl = `data:image/png;base64,${imageBytes}`;
      return this.sanitizer.bypassSecurityTrustUrl(dataUrl);
    } catch (error) {
      console.error('Error sanitizing image:', error);
      return this.sanitizer.bypassSecurityTrustUrl(''); // Return a safe, empty URL or handle the error accordingly
    }
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
        console.log(settings);
        console.log(this.recipeRating)
        this.profileService.createRating(this.recipeRating)
          .subscribe({
              next: data => {
                  this.notification.success("Successfully rated new recipe!");
                  window.location.reload();
              },
              error: error => {
                console.log(error)
                console.error(error.message, error);
                let title = "Could not rate recipe!";
                this.notification.error(this.errorFormatter.format(error), title, {
                  enableHtml: true,
                  timeOut: 5000,
                });
              }
            }
          );
      });
  }

  goToRecipe(recipeId: number) {
    this.router.navigate(['/recipe', recipeId]);
  }
}
