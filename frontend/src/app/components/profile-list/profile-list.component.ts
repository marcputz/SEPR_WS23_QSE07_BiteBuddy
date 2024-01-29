import {Component} from '@angular/core';
import {
  ProfileDetailDto, ProfileDto,
  ProfileSearch,
  ProfileSearchResultDto
} from '../../dtos/profileDto';
import {ProfileService} from '../../services/profile.service';
import {ToastrService} from 'ngx-toastr';
import {UserSettingsDto} from '../../dtos/userSettingsDto';
import {UserService} from '../../services/user.service';
import {ActivatedRoute, Router} from '@angular/router';
import {debounceTime, Subject} from 'rxjs';
import {ErrorHandler} from "../../services/errorHandler";
import {ImageHandler} from '../../utils/imageHandler';
import {SafeUrl} from '@angular/platform-browser';

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
  activeprofileId: number;
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
    private authService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private errorHandler: ErrorHandler,
    private imageHandler: ImageHandler,
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
        this.activeprofileId = settings.activeProfileId;
        this.reloadOwnProfiles();
        this.reloadDiscoverProfiles();
      },
      error => {

        let errorObj = this.errorHandler.getErrorObject(error);
        this.errorHandler.handleApiError(errorObj);

      }
    );
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
      },
      error: err => {

        let errorObj = this.errorHandler.getErrorObject(err);
        this.errorHandler.handleApiError(errorObj);

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
    let profileName: string;
    for (let profile of this.discoverProfiles) {
      if (profile.id === profileId) {
        profileName = profile.name;
      }
    }

    for (let profile of this.ownProfiles) {
      if (profile.name === profileName) {
        console.warn("Unable to add profile: Profile with name '" + profileName + "' already exists");
        this.notification.warning("You already have a profile with this name");
        return;
      }
    }

    this.profileService.copyToOwn(profileId).subscribe({
      next:
        (newProfile: ProfileDetailDto) => {
          this.ownProfiles.push(newProfile);
          this.notification.success(`Profile ${newProfile.name} successfully added.`);
        },
      error: error => {
        let errorObj = this.errorHandler.getErrorObject(error);
        this.errorHandler.handleApiError(errorObj);
      }
    });
  }

  deleteProfile(profileId: number, profileName: string) {
    this.profileService.deleteProfile(profileId).subscribe({
      next: data => {
        this.notification.success(`Profile ${profileName} deleted.`);
        this.reloadProfiles(this.ownSearchParams, true);
      },
      error: error => {

        let errorObj = this.errorHandler.getErrorObject(error);
        this.errorHandler.handleApiError(errorObj);

      }
    });
  }

  setActiveProfile(profileId: number, profileName: string) {
    this.profileService.setActiveProfile(profileId).subscribe({
      next: data => {
        this.notification.success(`Profile ${profileName} successfully activated.`);
        this.activeprofileId = profileId;
      },
      error: error => {

        let errorObj = this.errorHandler.getErrorObject(error);
        this.errorHandler.handleApiError(errorObj);

      }
    });
  }

  getPictureUrl(userPictureArray: number[]): SafeUrl {
    if (userPictureArray === undefined) {
      return this.imageHandler.sanitizeUserImage('/assets/icons/user_default.png');
    } else {
      //this.loadPreviewPicture();
      return this.imageHandler.sanitizeUserImage(userPictureArray);
    }
  }
}
