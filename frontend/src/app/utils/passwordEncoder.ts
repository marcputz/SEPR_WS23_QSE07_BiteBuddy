import {Injectable} from "@angular/core";
import * as shajs from 'sha.js';

@Injectable({
  providedIn: 'root'
})

export class PasswordEncoder {
  constructor() {}

  encodePassword(rawPassword: string): string {
    return shajs('sha256').update(rawPassword).digest('hex');
  }
}
