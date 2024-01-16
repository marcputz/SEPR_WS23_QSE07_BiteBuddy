import {Component, OnInit} from '@angular/core';
import {ProfileService} from "../../services/profile.service";
import {MenuPlanCreateDto} from "../../dtos/menuplan/menuPlanCreateDto";
import {DatePipe} from "@angular/common";
import {MenuPlanService} from "../../services/menuplan.service";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";

@Component({
  selector: 'app-menu-plan',
  templateUrl: './menu-plan.component.html',
  styleUrls: ['./menu-plan.component.scss']
})
export class MenuPlanComponent implements OnInit {

  protected generateRequest: string | null = null;
  protected generateResponse: string | null = null;

  constructor(private menuPlanService: MenuPlanService, private profileService: ProfileService, private datePipe: DatePipe, protected router: Router, protected notifications: ToastrService) {
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
        this.generateResponse = "ERROR --- " + JSON.stringify(error);

        let errorObject;
        if (typeof error.error === 'object') {
          errorObject = error.error;
        } else {
          errorObject = error;
        }

        let status = errorObject.status;
        let message = errorObject.error;

        switch(status) {
          case 401:
            this.notifications.error("Please log in again", "Login Timeout");
            this.router.navigate(['/login']);
        }
      }
    )

  }
}
