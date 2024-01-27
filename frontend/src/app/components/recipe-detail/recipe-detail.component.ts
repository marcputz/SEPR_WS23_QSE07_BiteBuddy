import {Component, OnInit} from '@angular/core';
import {RecipeDetailsDto, RecipeRatingListsDto} from "../../dtos/recipe";
import {RecipeService} from "../../services/recipe.service";
import {ActivatedRoute, Router} from "@angular/router";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {UserService} from "../../services/user.service";
import {ProfileService} from "../../services/profile.service";
import {UserSettingsDto} from "../../dtos/userSettingsDto";
import {CheckRatingDto} from "../../dtos/profileDto";
import {PictureService} from "../../services/picture.service";
import {PictureDto} from "../../dtos/pictureDto";

@Component({
  selector: 'app-recipe-detail',
  templateUrl: './recipe-detail.component.html',
  styleUrls: ['./recipe-detail.component.scss']
})
export class RecipeDetailComponent implements OnInit {

  recipeDetails: RecipeDetailsDto = {
    name: "",
    creatorName: "",
    description: "",
    id: -1,
    ingredients: null,
    allergens: null,
    picture: null,
    pictureId: null
  }

  recipePicture: number[] = null;

  userId: number = -1;
  likes: number[] = [];
  dislikes: number[] = [];
  rating: number = -1;
  ratingStatus: String = "";

  constructor(
    private service: RecipeService,
    private authService: UserService,
    private profileService: ProfileService,
    private pictureService: PictureService,
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

        // load image
        this.pictureService.getPicture(this.recipeDetails.pictureId).subscribe({
          next: pictureDto => {
            this.recipePicture = pictureDto.data;
          },
          error: error => {
            console.error(error);
          }
        })
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
              if (recipeRatingListsDto.likes.includes(this.recipeDetails.id)) {
                this.rating = 1;
                this.ratingStatus = "Liked";
              }
              if (recipeRatingListsDto.dislikes.includes(this.recipeDetails.id)) {
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
