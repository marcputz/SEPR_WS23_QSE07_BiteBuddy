import {Component} from '@angular/core';
import {RecipeService} from "../../services/recipe.service";
import {RecipeListDto, RecipeSearch} from "../../dtos/recipe";
import {debounceTime, Subject} from "rxjs";
import {DomSanitizer, SafeUrl} from '@angular/platform-browser';

@Component({
  selector: 'app-recipe-list',
  templateUrl: './recipe-list.component.html',
  styleUrls: ['./recipe-list.component.scss']
})
export class RecipeListComponent {
  recipes: RecipeListDto[] = [];
  searchChangedObservable = new Subject<void>();
  searchParams: RecipeSearch = {
    creator: "",
    name: "",
  };

  constructor(
    private service: RecipeService, private sanitizer: DomSanitizer
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
    console.log(this.searchParams);

    this.service.search(this.searchParams).subscribe({
      next: data => {
        console.log(data);
        this.recipes = data;
      },
      error: err => {
        console.log('Error fetching recipes', err)
        // TODO notification service
      }
    })
  }

  sanitizeImage(imageBytes: any): SafeUrl {
    try {
      if (!imageBytes || imageBytes.length === 0) {
        throw new Error('Empty or undefined imageBytes');
      }

      const base64Image = btoa(String.fromCharCode.apply(null, new Uint8Array(imageBytes)));
      const dataUrl = `data:image/png;base64,${base64Image}`;
      return this.sanitizer.bypassSecurityTrustUrl(dataUrl);
    } catch (error) {
      console.error('Error sanitizing image:', error);
      return this.sanitizer.bypassSecurityTrustUrl(''); // Return a safe, empty URL or handle the error accordingly
    }
  }
}
