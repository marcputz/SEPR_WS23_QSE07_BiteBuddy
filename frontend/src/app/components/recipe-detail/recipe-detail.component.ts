import {Component, OnInit} from '@angular/core';
import {RecipeDetailsDto} from "../../dtos/recipe";
import {RecipeService} from "../../services/recipe.service";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-recipe-detail',
  templateUrl: './recipe-detail.component.html',
  styleUrls: ['./recipe-detail.component.scss']
})
export class RecipeDetailComponent implements OnInit{

  recipeDetails: RecipeDetailsDto = {
    name: "",
    description: "",
    id: -1
  }

  constructor(
    private service: RecipeService,
    private router: Router,
    private route: ActivatedRoute,
  ) {

  }
  ngOnInit(): void {
    const routeParams = this.route.snapshot.paramMap;
    this.recipeDetails.id = Number(routeParams.get('id'));
    this.service.getById(this.recipeDetails.id).subscribe(
      (recipeDetails: RecipeDetailsDto) => {
        this.recipeDetails = recipeDetails;
      },
    );
  }

}
