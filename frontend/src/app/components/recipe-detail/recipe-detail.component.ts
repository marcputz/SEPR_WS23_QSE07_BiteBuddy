import {Component, OnInit} from '@angular/core';
import {RecipeDetailsDto} from "../../dtos/recipe";
import {RecipeService} from "../../services/recipe.service";
import {ActivatedRoute, Router} from "@angular/router";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";

@Component({
  selector: 'app-recipe-detail',
  templateUrl: './recipe-detail.component.html',
  styleUrls: ['./recipe-detail.component.scss']
})
export class RecipeDetailComponent implements OnInit{

  recipeDetails: RecipeDetailsDto = {
    name: "",
    description: "",
    id: -1,
    ingredients: null,
    allergens: null,
    picture: null
  }

  constructor(
    private service: RecipeService,
    private router: Router,
    private route: ActivatedRoute,
    private sanitizer: DomSanitizer
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
  sanitizeImage(imageBytes: any): SafeUrl {
    try {
      if (!imageBytes || imageBytes.length === 0) {
        throw new Error('Empty or undefined imageBytes');
      }
      const dataUrl = `data:image/png;base64,${imageBytes}`;
      return this.sanitizer.bypassSecurityTrustUrl(dataUrl);
    } catch (error) {
      console.error('Error sanitizing image:', error);
      return this.sanitizer.bypassSecurityTrustUrl(''); // Return a safe, empty URL or handle the error accordingly
    }
  }

}
