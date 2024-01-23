import {Component, OnInit} from '@angular/core';
import {RecipeDetailsDto, RecipeRatingListsDto} from "../../dtos/recipe";
import {RecipeService} from "../../services/recipe.service";
import {ActivatedRoute, Router} from "@angular/router";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {AuthService} from "../../services/auth.service";
import {ProfileService} from "../../services/profile.service";
import {UserSettingsDto} from "../../dtos/userSettingsDto";
import {CheckRatingDto} from "../../dtos/profileDto";

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

  userId: number = -1;
  likes: number[] = [];
  dislikes: number[] = [];
  rating: number = -1;
  ratingStatus: String = "";
  constructor(
    private service: RecipeService,
    private authService: AuthService,
    private profileService: ProfileService,
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
        console.log(recipeDetails);
      },
    );

    this.authService.getUser().subscribe(
      (settings: UserSettingsDto) => {
        this.userId = settings.id;
        console.log(settings);
        this.profileService.getRatingLists(this.userId)
          .subscribe(
              (recipeRatingListsDto: RecipeRatingListsDto) => {
                this.likes = recipeRatingListsDto.likes;
                this.dislikes = recipeRatingListsDto.dislikes;
                if(recipeRatingListsDto.likes.includes(this.recipeDetails.id)){
                  this.rating = 1;
                  this.ratingStatus = "Liked";
                }
                if(recipeRatingListsDto.dislikes.includes(this.recipeDetails.id)){
                  this.rating = 0;
                  this.ratingStatus = "Disliked"
                }
            }
          );
      });
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
  isInteger(value: number): boolean {
    return Number.isInteger(value);
  }
}
