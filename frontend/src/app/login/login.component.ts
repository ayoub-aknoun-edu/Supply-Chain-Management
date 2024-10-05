import { Component } from '@angular/core';
import {Router} from "@angular/router";
import { trigger, transition, style, animate } from '@angular/animations';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {CookieService} from "ngx-cookie-service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0 , transform: 'translateY(20px)'}),
        animate('1000ms ease-out', style({ opacity: 1 , transform: 'translateY(0)'}))
      ])
    ])
  ]
})

export class LoginComponent {
  username: string = '';
  password: string = '';
  errorMessage: string | null = null;

  constructor(
    private router: Router,
    private http: HttpClient,
    private cookieService: CookieService // Inject cookie service
  ) {}

  onLogin(): void {
    const loginData = {
      username: this.username,
      password: this.password
    };

    // Set up headers
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'accept': '*/*'
    });

    // HTTP POST request to backend
    this.http.post<any>('http://localhost:8080/api/auth/login', loginData, { headers }).subscribe({
      next: (response) => {
        // Assuming the backend returns a JWT token in the response
        const token = response.token;
        const role = response.role;
        this.cookieService.set('jwtToken', token);  // Store JWT in cookies
        this.cookieService.set('username', this.username);  // Store username in cookies
        this.cookieService.set('role', role);  // Store role in cookies
        // Navigate to the product page upon successful login
        this.router.navigate(['/product']);
      },
      error: (error) => {
        // check status code
        if (error.status === 403) {
          // Handle 401 Unauthorized error
          this.errorMessage = 'Invalid credentials. Please try again.';
        } else {
          // Handle other errors
          this.errorMessage = 'An error occurred. Please try again later.';
        }
        alert(this.errorMessage)
      }
    });
  }
}


