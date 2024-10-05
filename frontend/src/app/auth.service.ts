import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from "@angular/router";
import {Observable} from "rxjs";
import {CookieService} from "ngx-cookie-service";

@Injectable({
  providedIn: 'root'
})
export class AuthService implements CanActivate {

  constructor(private router: Router, private cookieService: CookieService) { }

  isLoggedIn(): boolean {
    const token = this.cookieService.get('jwtToken');// or use a cookie service
    return !!token;  // returns true if token exists
  }

  canActivate(): boolean {
    if (this.isLoggedIn()) {
      return true;  // If user is logged in, allow route access
    } else {
      this.router.navigate(['/login']);  // If not, redirect to login
      return false;
    }
  }
}
