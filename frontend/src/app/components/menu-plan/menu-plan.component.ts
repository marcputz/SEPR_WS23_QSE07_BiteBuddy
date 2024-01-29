import {Component, OnInit} from '@angular/core';
import {debounceTime, Subject} from "rxjs";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {ToastrService} from "ngx-toastr";
import {MenuPlanService} from "../../services/menuplan.service";
import {MenuPlanDetailDto} from "../../dtos/menuplan/menuPlanDetailDto";
import {MenuPlanContentDetailDto} from "../../dtos/menuplan/menuPlanContentDetailDto";
import {RecipeListDto} from "../../dtos/recipe";
import {MenuPlanUpdateRecipeDto} from "../../dtos/menuplan/menuPlanUpdateRecipeDto";
import {PictureService} from "../../services/picture.service"
import {ProfileListDto} from "../../dtos/profileDto";
import {ProfileService} from "../../services/profile.service";
import {ErrorHandler} from "../../services/errorHandler";
import {UserService} from "../../services/user.service";


@Component({
  selector: 'app-menu-plan-lookup',
  templateUrl: './menu-plan.component.html',
  styleUrls: ['./menu-plan.component.scss']
})
export class MenuPlanComponent implements OnInit {

  selectedStartDate: Date;
  fridge: string[];
  menuplan: MenuPlanDetailDto;
  searchday: string = new Date().toString();
  menuplans: MenuPlanDetailDto[];
  maxTimeslots: number;
  contents: MenuPlanContentDetailDto[];
  searchChangedObservable = new Subject<void>();
  recipes: RecipeListDto[] = [];
  updateValue: MenuPlanUpdateRecipeDto;
  recipeImages: Map<RecipeListDto, number[]> = null;
  recipeImageAlts: Map<RecipeListDto, string> = null;



  constructor(
    private service: MenuPlanService,
    private profileService: ProfileService,
    private errorHandler: ErrorHandler,
    private userService: UserService,
    private sanitizer: DomSanitizer,
    private notification: ToastrService,
    private pictureService: PictureService,
  ) {
  }

  ngOnInit() {
    this.updateValue = null;
    this.searchday = new Date().toString();
    this.getMenuPlans();
    this.getMenuPlan();
    this.searchChangedObservable
      .pipe(debounceTime(300))
      .subscribe({next: () => this.getMenuPlan()});

    this.profileService.getAllProfilesOfUser().subscribe(response => {
      this.createUserProfiles = response;
    });

    // get active profile and pre-select
    this.userService.getUser().subscribe({
      next: data => {
        this.createSelectedProfile = data.activeProfileId;
      },
      error: error => {
        let errorObj = this.errorHandler.getErrorObject(error);
        this.errorHandler.handleApiError(errorObj);
      }
    });
  }

  onMenuPlanSubmit(): void {
    this.showCreateDialog = false;
    this.ngOnInit();
  }
  getMenuPlans() {
    this.service.getMenuPlans().subscribe({
      next: data => {
        this.menuplans = data;
      },
      error: err => {
        this.notification.error('Error fetching recipes', err)
      }
    })
  }

  getImageFor(recipe: RecipeListDto) {
    if (this.recipeImages.has(recipe)) {
      return this.sanitizeImage(this.recipeImages.get(recipe));
    } else {
      return this.sanitizer.bypassSecurityTrustUrl("assets/images/recipe_default.png");
    }
  }

  getImageAltFor(recipe: RecipeListDto) {
    if (this.recipeImageAlts.has(recipe)) {
      return this.recipeImageAlts.get(recipe);
    } else {
      return "Recipe Image";
    }
  }

  getMenuPlan() {
    this.service.getMenuPlanForDay(this.searchday).subscribe({
      next: data => {
        this.menuplan = data;
        this.contents = [...data.contents];
        this.contents.sort((a, b) => {
          if (a.day !== b.day) {
            return a.day - b.day;
          } else {
            return a.timeslot - b.timeslot;
          }
        });
        this.maxTimeslots = Math.max(...this.contents.map(content => content.timeslot)) + 1;

        this.recipeImages = new Map<RecipeListDto, number[]>();
        this.recipeImageAlts = new Map<RecipeListDto, string>();
        for (let dto of this.contents) {
          this.pictureService.getPicture(dto.recipe.pictureId).subscribe({
            next: (pictureDto) => {
              this.recipeImages.set(dto.recipe, pictureDto.data);
              this.recipeImageAlts.set(dto.recipe, pictureDto.description);
            },
            error: error => {
              let errorObj = this.errorHandler.getErrorObject(error);
              this.errorHandler.handleApiError(errorObj);
            }
          });
        }
      },
      error: err => {

        let errorObj = this.errorHandler.getErrorObject(err);
        this.errorHandler.handleApiError(errorObj);
      }
    })
  }

  onMenuPlanSelected(event: Event): void {
    const selectedMenuPlanValue = (event.target as HTMLSelectElement).value;
    this.searchday = selectedMenuPlanValue.replace(/^\d+/, '').replace(':', '').replace(/'/g, '');
    this.searchChanged();
    this.ngOnInit();
  }

  searchChanged(): void {
    this.searchChangedObservable.next();
  }

  rerollRecipe(c: MenuPlanContentDetailDto) {
    this.updateValue = new class implements MenuPlanUpdateRecipeDto {
      day: number;
      menuPlanId: number;
      timeslot: number;
      dislike: boolean
    }
    this.updateValue.day = c.day;
    this.updateValue.timeslot = c.timeslot;
    this.updateValue.menuPlanId = this.menuplan.id;
    this.updateValue.dislike = false;
    this.service.updateRecipeInMenuPlan(this.updateValue).subscribe({
      next: response => {
        this.getMenuPlan();
        this.notification.success("Successfully rerolled recipe!");
      },
      error: err => {
        // Handle the error case here
        let errorObj = this.errorHandler.getErrorObject(err);
        this.errorHandler.handleApiError(errorObj);
      }
    });
  }

  dislikeRecipe(c: MenuPlanContentDetailDto) {
    this.updateValue = new class implements MenuPlanUpdateRecipeDto {
      day: number;
      menuPlanId: number;
      timeslot: number;
      dislike: boolean
    }
    this.updateValue.day = c.day;
    this.updateValue.timeslot = c.timeslot;
    this.updateValue.menuPlanId = this.menuplan.id;
    this.updateValue.dislike = true;
    this.service.updateRecipeInMenuPlan(this.updateValue).subscribe({
      next: response => {
        this.getMenuPlan();
        this.notification.success("Successfully disliked recipe!");
      },
      error: err => {
        // Handle the error case here
        let errorObj = this.errorHandler.getErrorObject(err);
        this.errorHandler.handleApiError(errorObj);
      }
    });
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

  formatDateLocally(inputDate: string, plusDays: number): string {
    const months = [
      'January', 'February', 'March', 'April',
      'May', 'June', 'July', 'August',
      'September', 'October', 'November', 'December'
    ];

    let [year, month, day] = inputDate.split('-').map(Number);
    // Adjust the date by adding plusDays
    day = day + plusDays;

    // Check if there's an overflow to the next month
    while (day > this.getDaysInMonth(year, month)) {
      day -= this.getDaysInMonth(year, month);
      month++;

      // Check if there's an overflow to the next year
      if (month > 12) {
        month = 1;
        year++;
      }
    }
    let monthName = months[month - 1];
    let dayOrdinal = this.getDayOrdinal(day);
    return `${monthName} ${day}${dayOrdinal}`;
  }

  getDaysInMonth(year: number, month: number): number {
    // Function to get the number of days in a given month
    return new Date(year, month, 0).getDate();
  }

  getDayOrdinal(day: number): string {
    if (day >= 11 && day <= 13) {
      return 'th';
    }

    switch (day % 10) {
      case 1:
        return 'st';
      case 2:
        return 'nd';
      case 3:
        return 'rd';
      default:
        return 'th';
    }
  }

  protected showCreateDialog: boolean = false;

  protected createUserProfiles: ProfileListDto[];
  protected createSelectedProfile: number | null = null;
}



