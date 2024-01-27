import {Injectable} from "@angular/core";
import {ToastrService} from "ngx-toastr";
import {ErrorDto} from "../dtos/errorDto";
import {toNumber} from "lodash";

@Injectable({
  providedIn: 'root'
})

export class ApiErrorHandler {

  constructor(protected notifications: ToastrService) {}

  handleApiError(error) {

    let errorObject = null;

    if (error.error != undefined) {
      // this means the error has a body

      let errorJson = JSON.parse(error.error);
      if (errorJson["statusCode"] !== undefined && errorJson["statusDescription"] !== undefined && errorJson["statusText"] != undefined) {
        // this is an errorDto object

        let errorDto: ErrorDto = new ErrorDto(errorJson["statusCode"], errorJson["statusText"], errorJson["statusDescription"], errorJson["reason"]);

        if (errorDto != undefined) {
          errorObject = errorDto;
        } else {
          // conversion to errror dto was not successfull
          errorObject = error.error;
        }

      } else {
        // this is another kind of error
        errorObject = error.error;
      }
    } else {
      // general error, probably from the browser directly
    }

    console.error("BiteBuddy Service replied with an error: ", errorObject);

    // TODO: handle the error object and check for it's status code (and possibly other properties)
  }
}
