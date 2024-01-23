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

@Component({
  selector: 'app-menu-plan-lookup',
  templateUrl: './menu-plan-lookup.component.html',
  styleUrls: ['./menu-plan-lookup.component.scss']
})
export class MenuPlanLookupComponent {

  menuplan: MenuPlanDetailDto;
  searchday: string;
  menuplans: MenuPlanDetailDto[];
  maxTimeslots: number;
  contents: MenuPlanContentDetailDto[];
  searchChangedObservable = new Subject<void>();
  recipes: RecipeListDto[] = [];



  constructor(
    private service: MenuPlanService,
    private sanitizer: DomSanitizer,
    private notification: ToastrService,
  ) {
  }
  ngOnInit() {
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

}
