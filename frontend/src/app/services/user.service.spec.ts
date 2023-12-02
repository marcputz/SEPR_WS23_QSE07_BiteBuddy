import {TestBed} from '@angular/core/testing';

import {UserService} from './user.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';

describe('AuthService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule, RouterTestingModule, ReactiveFormsModule],
  }));

  it('should be created', () => {
    const service: UserService = TestBed.inject(UserService);
    expect(service).toBeTruthy();
  });
});
