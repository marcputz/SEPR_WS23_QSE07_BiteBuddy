import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {ProfileService} from "../../services/profile.service";
import {MenuPlanCreateDto} from "../../dtos/menuplan/menuPlanCreateDto";
import {formatDate} from "@angular/common";
import {MenuPlanService} from "../../services/menuplan.service";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {RecipeService} from "../../services/recipe.service";
import {of} from "rxjs";
import {ProfileListDto} from "../../dtos/profileDto";
import {ErrorHandler} from "../../services/errorHandler";

@Component({
  selector: 'app-menu-plan-create',
  templateUrl: './menu-plan-create-component.html',
  styleUrls: ['./menu-plan-create-component.scss']
})
export class MenuPlanCreateComponent implements OnInit {

  @Output() submitClicked: EventEmitter<any> = new EventEmitter();
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

  constructor(private service: MenuPlanService,
              private profileService: ProfileService,
              protected router: Router,
              protected notification: ToastrService,
              private recipeService: RecipeService,
              private errorHandler: ErrorHandler,) {
  }

  ngOnInit() {
    this.profileService.getAllProfilesOfUser().subscribe(response => {
      this.userProfiles = response;
    });
  }

  onClickGenerate() {
    if (this.selectedProfile == null) {
      this.notification.warning("Please select a profile");
      return;
    }

    this.createDto.fromTime = Date.now().toString();
    this.createDto.untilTime = Date.now().toString();
    this.createDto.profileId = -1;

    let fromDate = new Date();
    let untilDate = new Date();
    untilDate.setDate(untilDate.getDate() + 6);

    this.createDto.fromTime = formatDate(fromDate, 'yyyy-MM-dd', 'en_US');
    this.createDto.untilTime = formatDate(untilDate, 'yyyy-MM-dd', 'en_US');
    this.createDto.profileId = this.selectedProfile;

    this.service.generateMenuPlan(this.createDto).subscribe(
      data => {
        this.notification.success("Created Menu Plan");
        this.submitClicked.emit();
        this.router.navigate(["/menuplanLookup"])
      },
      error => {
        let errorObj = this.errorHandler.getErrorObject(error);

        switch (errorObj.status) {
          case 409: // conflict
            this.notification.warning("You already have an active menu plan active for now, please wait for the current plan to end");
            break;
          default:
            this.errorHandler.handleApiError(errorObj);
            break;
        }
      }
    )
  }

  addIngredientToFridge(ingredient) {
    if (ingredient.value.trim().length > 0) {
      this.createDto.fridge.push(ingredient.value)
    }
  }

  removeIngredient(ingredient) {
    const indexToRemove = this.createDto.fridge.indexOf(ingredient);
    this.createDto.fridge.splice(indexToRemove, 1);
  }

  formatIngredient(ingredient: String | null) {
    return ingredient ?? '';
  }

  ingredientSuggestions = (input: string) => (input === '')
    ? of([])
    : this.recipeService.searchRecipeIngredientsMatching(input);
}
