import {Component} from '@angular/core';
import {RecipeService} from "../../services/recipe.service";
import {RecipeListDto, RecipeSearch, RecipeSearchResultDto} from "../../dtos/recipe";
import {debounceTime, Subject} from "rxjs";
import {DomSanitizer, SafeUrl} from '@angular/platform-browser';
import {ToastrService} from "ngx-toastr";

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

  constructor(
    private service: RecipeService,
    private sanitizer: DomSanitizer,
    private notification: ToastrService,
  ) {
  }

  ngOnInit() {
    this.reloadRecipes();

    this.searchChangedObservable
      .pipe(debounceTime(300))
      .subscribe({next: () => this.reloadRecipes()});
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
}
