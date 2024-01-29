import { Component } from '@angular/core';
import {ProfileService} from "../../services/profile.service";
import {ActivatedRoute, Router} from "@angular/router";
import {DomSanitizer} from "@angular/platform-browser";
import {ProfileDetailDto} from "../../dtos/profileDto";

@Component({
  selector: 'app-profile-details',
  templateUrl: './profile-details.component.html',
  styleUrls: ['./profile-details.component.scss']
})
export class ProfileDetailsComponent {

  profileDetails: ProfileDetailDto = {
    id: -1,
    name: "",
    allergens: [],
    ingredients: [],
    liked: [],
    disliked: [],
    user: "",
    userId: -1,
    userPicture: []
  }

  userId: number = -1;
  likes: number[] = [];
  dislikes: number[] = [];
  rating: number = -1;
  ratingStatus: String = "";

  constructor(
    private service: ProfileService,
    private router: Router,
    private route: ActivatedRoute,
    private sanitizer: DomSanitizer
  ) {

  }
  ngOnInit(): void {
    const routeParams = this.route.snapshot.paramMap;
    this.profileDetails.id = Number(routeParams.get('id'));
    this.service.getProfileDetails(this.profileDetails.id).subscribe(
      (profileDetails: ProfileDetailDto) => {
        this.profileDetails = profileDetails;
        console.log(profileDetails);
      },
    );
  }
}


