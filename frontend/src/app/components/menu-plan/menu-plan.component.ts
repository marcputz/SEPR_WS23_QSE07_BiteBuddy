import {Component, OnInit} from '@angular/core';
import {ProfileService} from "../../services/profile.service";
import {MenuPlanCreateDto} from "../../dtos/menuplan/menuPlanCreateDto";
import {DatePipe} from "@angular/common";
import {MenuPlanService} from "../../services/menuplan.service";

@Component({
  selector: 'app-menu-plan',
  templateUrl: './menu-plan.component.html',
  styleUrls: ['./menu-plan.component.scss']
})
export class MenuPlanComponent implements OnInit {

  protected generateRequest: string | null = null;
  protected generateResponse: string | null = null;

  constructor(private menuPlanService: MenuPlanService, private profileService: ProfileService, private datePipe: DatePipe) {
  }

  ngOnInit() {

  }

  onClickGenerate() {

    console.log("generate clicked");

    let fromDate = new Date();
    let untilDate = new Date();
    untilDate.setDate(untilDate.getDate() + 6);

    let createDto: MenuPlanCreateDto = {
      profileId: 0,
      fromTime: this.datePipe.transform(fromDate, 'yyyy-MM-dd'),
      untilTime: this.datePipe.transform(untilDate, 'yyyy-MM-dd')
    }

    this.generateRequest = JSON.stringify(createDto);

    this.menuPlanService.generateMenuPlan(createDto).subscribe(
      data => {
        this.generateResponse = JSON.stringify(data);
      },
      error => {
        console.error(error);
      }
    )

  }
}
