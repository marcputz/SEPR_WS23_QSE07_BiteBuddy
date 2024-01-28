import {Component} from '@angular/core';
import {
  ProfileDetailDto, ProfileDto,
  ProfileSearch,
  ProfileSearchResultDto
} from '../../dtos/profileDto';
import {ProfileService} from '../../services/profile.service';
import {ToastrService} from 'ngx-toastr';
import {UserSettingsDto} from '../../dtos/userSettingsDto';
import {AuthService} from '../../services/auth.service';
import {ActivatedRoute} from '@angular/router';
import {debounceTime, Subject} from 'rxjs';

@Component({
  selector: 'app-profile-list',
  templateUrl: './profile-list.component.html',
  styleUrls: ['./profile-list.component.scss']
})
export class ProfileListComponent {
  title: string = "Profile List";
  fragment: string;
  scrolledToAnchor: boolean;
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

  searchOwnChangedObservable = new Subject<void>();
  searchDiscoverChangedObservable = new Subject<void>();

  constructor(
    private profileService: ProfileService,
    private notification: ToastrService,
    private authService: AuthService,
    private route: ActivatedRoute,
  ) {
  }

  ngOnInit(): void {
    this.searchOwnChangedObservable
      .pipe(debounceTime(300))
      .subscribe({next: () => this.reloadOwnProfiles()});
    this.searchDiscoverChangedObservable
      .pipe(debounceTime(300))
      .subscribe({next: () => this.reloadDiscoverProfiles()});
    this.ownProfiles = null;
    this.ownSearchParams = {
      name: '', // default value or empty string
      creator: '', // default value or empty string
      page: 0, // default starting page
      entriesPerPage: 12, // default number of entries per page
      ownProfiles: true
    };
    this.discoverSearchParams = {
      name: '', // default value or empty string
      creator: '', // default value or empty string
      page: 0, // default starting page
      entriesPerPage: 6, // default number of entries per page
      ownProfiles: false
    };
    this.authService.getUser().subscribe(
      (settings: UserSettingsDto) => {
        this.currentUserName = settings.nickname;
        console.log('Got UserId: ' + settings.id);
        this.reloadOwnProfiles();
        this.reloadDiscoverProfiles();
      });
    this.route.fragment.subscribe(fragment => {
      this.fragment = fragment;
    });
  }

  scrollToAnchor(): void {
    if (!this.scrolledToAnchor) {
      setTimeout(() => { // Added a slight delay to ensure DOM updates
        const element = document.querySelector('#' + this.fragment);
        if (element) {
          element.scrollIntoView();
        }
        this.scrolledToAnchor = true;
      }, 100); // You can adjust this delay as needed
    }
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
      next: async data => {
        if (ownProfiles) {
          this.ownSearchResult = data;
          this.ownProfiles = data.profiles;
          this.ownMaxPages = data.numberOfPages;
          this.ownSearchParams.page = data.page;
          this.scrollToAnchor();
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

  searchOwnChanged() {
    this.searchOwnChangedObservable.next();
  }

  searchDiscoverChanged() {
    this.searchDiscoverChangedObservable.next();
  }

  pageChanger(newPageNumber: number) {
    this.discoverSearchParams.page = newPageNumber;
    this.reloadDiscoverProfiles()
  }

  onImageLoad(event: any, userId: number): void {
    if (userId) {
      event.target.src = `/user/${userId}.jpg`;
    }
  }

  addToOwn(profileId: number) {
    this.profileService.copyToOwn(profileId).subscribe(
      (newProfile: ProfileDetailDto) => {
        this.ownProfiles.push(newProfile);
      });
  }

  deleteProfile(profileId: number, profileName: string) {
    this.profileService.deleteProfile(profileId).subscribe({
      next: data => {
        this.notification.success(`Profile ${profileName} successfully deleted.`);
        this.reloadProfiles(this.ownSearchParams, true);
      },
      error: error => {
        console.error('Error deleting profile', error);
        const errorMessage = error?.error.reason || 'Unknown error occured';
        this.notification.error(`Error deleting profile: ${errorMessage}`);
      }
    });
  }
}
