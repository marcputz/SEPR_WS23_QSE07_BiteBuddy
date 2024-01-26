import { Component } from '@angular/core';
import {debounceTime, Subject} from "rxjs";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {ToastrService} from "ngx-toastr";
import {MenuPlanService} from "../../services/menuplan.service";
import {MenuPlanDetailDto} from "../../dtos/menuplan/menuPlanDetailDto";
import {MenuPlanContentDetailDto} from "../../dtos/menuplan/menuPlanContentDetailDto";
import {RecipeService} from "../../services/recipe.service";
import {RecipeListDto} from "../../dtos/recipe";
import {forEach} from "lodash";
import {Logger} from "jasmine-spec-reporter/built/display/logger";
import {MenuPlanUpdateRecipeDto} from "../../dtos/menuplan/menuPlanUpdateRecipeDto";
import {PictureService} from "../../services/picture.service"


@Component({
  selector: 'app-menu-plan-lookup',
  templateUrl: './menu-plan-lookup.component.html',
  styleUrls: ['./menu-plan-lookup.component.scss']
})
export class MenuPlanLookupComponent {

  menuplan: MenuPlanDetailDto;
  searchday: string = new Date().toString();
  menuplans: MenuPlanDetailDto[];
  maxTimeslots: number;
  contents: MenuPlanContentDetailDto[];
  searchChangedObservable = new Subject<void>();
  recipes: RecipeListDto[] = [];
  updateValue: MenuPlanUpdateRecipeDto;
  recipeImages: Map<RecipeListDto, number[]> = null;




  constructor(
    private service: MenuPlanService,
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
  }

  getMenuPlans(){
    this.service.getMenuPlans().subscribe({
      next: data => {
        this.menuplans = data;
        console.log("plans available plans: " + data.length);
      },
      error: err => {
        this.notification.error('Error fetching recipes', err)
      }
    })
  }

  getImageFor(recipe: RecipeListDto) {
    if (this.recipeImages.has(recipe)) {
      return this.recipeImages.get(recipe);
    } else {
      return null;
    }
  }
  getMenuPlan() {
    console.log("before sending getMenuPlan date: " + this.searchday);
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
        for (let dto of this.contents) {
          this.pictureService.getPicture(dto.recipe.pictureId).subscribe({
            next: (pictureDto) => {
              this.recipeImages.set(dto.recipe, pictureDto.data);
            },
            error: error => {
              console.error(error);
            }
          });
        }
      },
      error: err => {
        this.notification.error('Error fetching recipes for 1 menuplan', err)
      }
    })
  }
  onMenuPlanSelected(event: Event): void {
    const selectedMenuPlanValue = (event.target as HTMLSelectElement).value;
    this.searchday = selectedMenuPlanValue.replace(/^\d+/, '').replace(':', '').replace(/'/g, '');
    this.searchChanged();
  }
  searchChanged(): void {
    this.searchChangedObservable.next();
  }

  likeRecipe( c: MenuPlanContentDetailDto) {
    // Call your like function logic here

    console.log('Recipe liked!');
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
    this.service.updateRecepyInMenuPlan(this.updateValue).subscribe({
      next: data => {
        console.log("plans available plans: ");
      },
      error: err => {
        this.notification.error('Error fetching recipes', err)
      }
    })
    this.getMenuPlan();
    this.searchChanged();
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
    this.service.updateRecepyInMenuPlan(this.updateValue).subscribe({
      next: data => {
        console.log("plans available plans: ");
      },
      error: err => {
        this.notification.error('Error fetching recipes', err)
      }
    })
    this.getMenuPlan();
    this.searchChanged();
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

  formatDate(inputDate: string, plusDay: number): string {
    const months = [
      'January', 'February', 'March', 'April',
      'May', 'June', 'July', 'August',
      'September', 'October', 'November', 'December'
    ];

    let [year, month, day] = inputDate.split('-').map(Number);
    const monthName = months[month - 1];
    day = day + plusDay;
    const dayOrdinal = this.getDayOrdinal(day);

    return `${monthName} ${day}${dayOrdinal}`;
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

}
