import {Component} from '@angular/core';
import {RecipeService} from "../../services/recipe.service";
import {RecipeListDto, RecipeSearch} from "../../dtos/recipe";
import {debounceTime, Subject} from "rxjs";

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
    maxCount: 5
  };

  constructor(
    private service: RecipeService
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
}
