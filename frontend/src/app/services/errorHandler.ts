import {Injectable} from "@angular/core";
import {ToastrService} from "ngx-toastr";
import {ErrorDto} from "../dtos/errorDto";
import {Router} from "@angular/router";
import {UserService} from "./user.service";

@Injectable({
  providedIn: 'root'
})

export class ErrorHandler {

  constructor(protected router: Router,
              protected notifications: ToastrService) {}

  getErrorObject(error: any): ErrorDto {

    let errorStatus: number;
    let errorText: string;
    let errorMessage: string;
    let reason: string;

    // set error status code
    errorStatus = error.status;

    // set error text
    errorText = error.statusText;
    if (errorText == undefined || errorText == "OK") {
      errorText = this.statusCodeToStatusText(errorStatus);
    }

    if (error.error != undefined) {
      // error has a body
      let errorBody = error.error;

      if (errorBody.statusCode != undefined && errorBody.statusText != undefined) {
        // error dto was parsed correctly

        errorStatus = errorBody.statusCode;
        errorText = errorBody.statusText;
        errorMessage = errorBody.statusDescription;
        reason = errorBody.reason;

      }
    }

    return new ErrorDto(errorStatus, errorText, errorMessage, reason);

  }

  handleApiError(error: ErrorDto) {
    console.warn("BiteBuddy Service replied with an error: ", error);

    switch (error.status) {
      case 400: // bad request
        this.notifications.warning("We couldn't process your request, as its format wasn't known to our servers. Please check your inputs and try again!");
        break;
      case 401: // Unauthorized: logout at first then login again
        this.notifications.info("You were forcefully logged-out by the server, please log in again");
        //Cannot make logout because of circular dependencies in the classes with userservice
        this.router.navigate(['login']);
        break;
      case 404: // not found
        this.notifications.warning("We couldn't find the resource you were looking for, please check your inputs and try again!");
        break;
      case 409: // conflict
        this.notifications.warning("Your input is in conflict with already existing data, please change your inputs and try again!");
        break;
      case 422: // validation
        this.notifications.warning("Your inputs are invalid or do not match the requirements, please change your inputs and try again!");
        break;
      case 500: // internal server error
        this.notifications.error("Something went wrong on our side, please try again later!");
        break;
      case 503: // service unavailable
        this.notifications.error("This function isn't available right now, please try again later!");
        break;


      // TODO: handle other status codes

      default:
        this.notifications.error(error.reason != undefined ? error.reason : "Something went wrong, please try again!");
        break;
    }
  }

  protected statusCodeToStatusText(code: number): string {
    switch (code) {
      case 100: return "CONTINUE";
      case 101: return "SWITCHING_PROTOCOLS";
      case 102: return "PROCESSING";
      case 103: return "EARLY_HINTS";
      case 200: return "OK";
      case 201: return "CREATED";
      case 202: return "ACCEPTED";
      case 203: return "NON-AUTHORITATIVE_INFORMATION";
      case 204: return "NO_CONTENT";
      case 205: return "RESET_CONTENT";
      case 206: return "PARTIAL_CONTENT";
      case 207: return "MULTI-STATUS";
      case 208: return "ALREADY_REPORTED";
      case 226: return "IM_USED";
      case 300: return "MULTIPLE_CHOICES";
      case 301: return "MOVED_PERMANENTLY";
      case 302: return "FOUND";
      case 303: return "SEE_OTHER";
      case 304: return "NOT_MODIFIED";
      case 305: return "USE_PROXY";
      case 307: return "TEMPORARY_REDIRECT";
      case 308: return "PERMANENT_REDIRECT";
      case 400: return "BAD_REQUEST";
      case 401: return "UNAUTHORIZED";
      case 402: return "PAYMENT_REQUIRED";
      case 403: return "FORBIDDEN";
      case 404: return "NOT_FOUND";
      case 405: return "METHOD_NOT_ALLOWED";
      case 406: return "NOT_ACCEPTABLE";
      case 407: return "PROXY_AUTHENTICATION_REQUIRED";
      case 408: return "REQUEST_TIMEOUT";
      case 409: return "CONFLICT";
      case 410: return "GONE";
      case 411: return "LENGTH_REQUIRED";
      case 412: return "PRECONDITION_FAILED";
      case 413: return "PAYLOAD_TOO_LARGE";
      case 414: return "URI_TOO_LONG";
      case 415: return "UNSUPPORTED_MEDIA_TYPE";
      case 416: return "RANGE_NOT_SATISFIABLE";
      case 417: return "EXPECTATION_FAILED";
      case 418: return "IM_A_TEAPOT";
      case 421: return "MISDIRECTED_REQUEST";
      case 422: return "UNPROCESSABLE_CONTENT";
      case 423: return "LOCKED";
      case 424: return "FAILED_DEPENDENCY";
      case 425: return "TOO_EARLY";
      case 426: return "UPGRADE_REQUIRED";
      case 428: return "PRECONDITION_REQUIRED";
      case 429: return "TOO_MANY_REQUESTS";
      case 431: return "REQUEST_HEADER_FIELDS_TOO_LARGE";
      case 451: return "UNAVAILABLE_FOR_LEGAL_REASONS";
      case 500: return "INTERNAL_SERVER_ERROR";
      case 501: return "NOT_IMPLEMENTED";
      case 502: return "BAD_GATEWAY";
      case 503: return "SERVICE_UNAVAILABLE";
      case 504: return "GATEWAY_TIMEOUT";
      case 505: return "HTTP_VERSION_NOT_SUPPORTED";
      case 506: return "VARIANT_ALSO_NEGOTIATES";
      case 507: return "INSUFFICIENT_STORAGE";
      case 508: return "LOOP_DETECTED";
      case 510: return "NOT_EXTENDED";
      case 511: return "NETWORK_AUTHENTICATION_REQUIRED";
      default: return "";
    }
  }
}
