import {Component, OnInit} from '@angular/core';
import {ProfileService} from "../../services/profile.service";
import {MenuPlanCreateDto} from "../../dtos/menuplan/menuPlanCreateDto";
import {DatePipe} from "@angular/common";
import {MenuPlanService} from "../../services/menuplan.service";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {RecipeService} from "../../services/recipe.service";
import {of} from "rxjs";
import {ProfileDto, ProfileListDto} from "../../dtos/profileDto";

@Component({
  selector: 'app-menu-plan',
  templateUrl: './menu-plan.component.html',
  styleUrls: ['./menu-plan.component.scss']
})
export class MenuPlanComponent implements OnInit {

  protected generateRequest: string | null = null;
  protected generateResponse: string | null = null;

  protected fromDate: string | null = "2024-01-17";
  protected untilDate: string | null = "2024-01-23";

  protected userProfiles: ProfileListDto[];
  protected selectedProfile: number | null = null;

  createDto: MenuPlanCreateDto = {
    profileId: this.selectedProfile,
    fromTime: this.fromDate,
    untilTime: this.untilDate,
    fridge: []
  }
  ingredient: string = '';

  constructor(private menuPlanService: MenuPlanService, private profileService: ProfileService, private datePipe: DatePipe, protected router: Router, protected notifications: ToastrService, private recipeService: RecipeService) {
  }

  ngOnInit() {
    this.profileService.getAllProfilesOfUser().subscribe(response => {
      this.userProfiles = response;
    });
  }

  onClickGenerate() {

    if (this.selectedProfile == null) {
      return;
    }

    this.createDto.fromTime = this.fromDate;
    this.createDto.untilTime = this.untilDate;
    this.createDto.profileId = this.selectedProfile;

    this.generateRequest = JSON.stringify(this.createDto);

    this.menuPlanService.generateMenuPlan(this.createDto).subscribe(
      data => {
        this.generateResponse = JSON.stringify(data);
      },
      error => {
        console.error(error);
        this.generateResponse = "ERROR --- " + JSON.stringify(error);

        let errorObject;
        if (typeof error.error === 'object') {
          errorObject = error.error;
        } else {
          errorObject = error;
        }

        let status = errorObject.status;
        let message = errorObject.error;

        switch (status) {
          case 401:
            this.notifications.error("Please log in again", "Login Timeout");
            this.router.navigate(['/login']);
        }
      }
    )
  }

  addIngredientToFridge(ingredient) {
    this.createDto.fridge.push(ingredient.value)
  }

  formatIngredient(ingredient: String | null) {
    return ingredient ?? '';
  }

  removeIngredient(ingredient) {
    const indexToRemove = this.createDto.fridge.indexOf(ingredient);
    this.createDto.fridge.splice(indexToRemove, 1);
  }

  ingredientSuggestions = (input: string) => (input === '')
    ? of([])
    : this.recipeService.searchRecipeIngredientsMatching(input);
}
