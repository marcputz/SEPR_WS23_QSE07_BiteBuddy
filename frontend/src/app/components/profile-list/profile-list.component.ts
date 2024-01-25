import {Component} from '@angular/core';
import {
  ProfileDetailDto,
  ProfileSearch,
  ProfileSearchResultDto
} from '../../dtos/profileDto';
import {ProfileService} from '../../services/profile.service';
import {ToastrService} from 'ngx-toastr';
import {UserSettingsDto} from '../../dtos/userSettingsDto';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-profile-list',
  templateUrl: './profile-list.component.html',
  styleUrls: ['./profile-list.component.scss']
})
export class ProfileListComponent {
  title: string = "Profile List";
  currentUserName = "";
  ownMaxPages: number = 5;

  ownSearchParams: ProfileSearch;
  ownSearchResult: ProfileSearchResultDto;
  ownProfiles: ProfileDetailDto[];

  discoverMaxPages: number = 5;
  discoverUserName = "";
  discoverSearchParams: ProfileSearch;
  discoverSearchResult: ProfileSearchResultDto;
  discoverProfiles: ProfileDetailDto[];

  constructor(
    private profileService: ProfileService,
    private notification: ToastrService,
    private authService: AuthService,
  ) {
  }

  ngOnInit(): void {
    //TODO Load profiles here (e.g., from a service)
    this.ownProfiles = null;
    this.ownSearchParams = {
      name: '', // default value or empty string
      creator: '', // default value or empty string
      page: 0, // default starting page
      entriesPerPage: 12, // default number of entries per page
      ownProfiles: true
    };
    this.discoverSearchParams = this.ownSearchParams;
    this.discoverSearchParams.ownProfiles = false;
    this.authService.getUser().subscribe(
      (settings: UserSettingsDto) => {
        this.currentUserName = settings.nickname;
        console.log('Got UserId: ' + settings.id);
        this.reloadOwnProfiles();
        this.reloadDiscoverProfiles();
      });
  }


  reloadOwnProfiles() {
    this.reloadProfiles(this.ownSearchParams, true);
  }


  reloadDiscoverProfiles() {
    this.reloadProfiles(this.discoverSearchParams, false);
  }

  reloadProfiles(searchParams: ProfileSearch, ownProfiles: boolean) {
    searchParams.ownProfiles = ownProfiles;
    this.profileService.search(searchParams).subscribe({
      next: data => {
        if (ownProfiles) {
          this.ownSearchResult = data;
          this.ownProfiles = data.profiles;
          this.ownMaxPages = data.numberOfPages;
          this.ownSearchParams.page = data.page;
        } else {
          this.discoverSearchResult = data;
          this.discoverProfiles = data.profiles;
          this.discoverMaxPages = data.numberOfPages;
          this.discoverSearchParams.page = data.page;
        }
        console.log("number of pages: " + data.numberOfPages);
        console.log("profiles available: " + data.profiles.length);
      },
      error: err => {
        this.notification.error('Error fetching profiles', err)
      }
    })
  }

  searchChanged() {
  }

  pageChanger(newPageNumber: number) {
    this.ownSearchParams.page = newPageNumber;
    this.reloadOwnProfiles()
  }

  pageCounter(change: number) {
    this.ownSearchParams.page += change;
    this.reloadOwnProfiles()
  }

  onImageLoad(event: any, userId: number): void {
    if (userId) {
      event.target.src = `/user/${userId}.jpg`;
    }
  }
}
