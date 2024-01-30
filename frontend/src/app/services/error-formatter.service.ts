import {Injectable, SecurityContext} from '@angular/core';
import {DomSanitizer} from '@angular/platform-browser';

@Injectable({
  providedIn: 'root'
})
export class ErrorFormatterService {

  constructor(
    private domSanitizer: DomSanitizer,
  ) { }

  /**
   * Format the error message from the backend to a user string
   *
   * @param error the error from the backend to format
   * @return the formatted error message for the user
   */
  format(error: any): string {
    let message = this.domSanitizer.sanitize(SecurityContext.HTML, error.error.message) ?? '';
    if (!!error.error.errors) {
      message += ': ';
      for (const e of error.error.errors) {
        /* Use Angular's DomSanitizer to strip dangerous parts out of the HTML
         * before putting it into the error message.
         * Toastr already does this, but it can't hurt to do here too,
         * in case the library ever fails.
         */
        const sanE = this.domSanitizer.sanitize(SecurityContext.HTML, e);
        message += ` ${sanE},`;
      }
      message += ' ';
    } else if (error.error != null && error.status != 0) {
      message = error.error.message + " ";
    } else {
      message += '.';
    }
    return message;
  }
}
