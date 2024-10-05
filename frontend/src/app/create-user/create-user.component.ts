import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {CookieService} from "ngx-cookie-service";
import { FormGroup, FormControl, Validators } from '@angular/forms';

@Component({
  selector: 'app-create-user',
  templateUrl: './create-user.component.html',
  styleUrls: ['./create-user.component.css']
})

export class CreateUserComponent implements OnInit{
  username: string = this.cookieService.get('username');
  role: string = this.cookieService.get('role');
  constructor(private http: HttpClient, private cookieService: CookieService) {}

  ngOnInit() {
    this.addUserForm = new FormGroup({
      username: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(6)]),
      role: new FormControl('USER', Validators.required)
    });
    // Fetch files when the component is initialized
    this.fetchUsers();
  }
  addUserForm: FormGroup | any;
  users: any[] = [];
  @ViewChild('closeModal') closeModalBtn: ElementRef | undefined

  errorMessage: string | null = null;

  onSubmit() {
    if (this.addUserForm.valid) {
      const token = this.cookieService.get('jwtToken');  // Get JWT from cookies

      const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`,  // Include JWT in headers
        'accept': '*/*'
      });

      // Prepare the form data
      let userData = { ...this.addUserForm.value, id: '', privateKey: '', publicKey: '' };
      console.log(userData);

      // Send the form data to the backend
      this.http.post('http://localhost:8080/api/auth/register', userData, { headers }).subscribe({
        next: (response) => {
          console.log('User registered successfully!');
          this.fetchUsers();
          this.closeModal();  // Close the modal after success
          this.addUserForm.reset({ role: 'USER' });  // Reset the form with default values
        },
        error: (error) => {
          console.log(error);
          alert('Failed to register user. Please try again.');
        }
      });
    } else {
      alert('Please fill out all required fields correctly.');
    }
  }

  closeModal() {
    // @ts-ignore
    this.closeModalBtn.nativeElement.click()
  }

  hasError(controlName: string, errorName: string) {
    return this.addUserForm.get(controlName)?.hasError(errorName) &&
      this.addUserForm.get(controlName)?.touched;
  }

  onLogout() {
    // Clear JWT token from cookies
    this.cookieService.delete('jwtToken');
    this.cookieService.delete('username');
  }

  fetchUsers() {
    const token = this.cookieService.get('jwtToken');  // Get JWT from cookies

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,  // Include JWT in headers
      'accept': '*/*'
    });

    // Send GET request to fetch files
    this.http.get<any[]>('http://localhost:8080/api/users', { headers }).subscribe({
      next: (response) => {
        this.users = response;  // Update the file list with the response
        // for each user, change role from MANAGER to ADMINISTRATOR
        this.users.forEach((user) => {
          if (user.role === 'MANAGER') {
            user.role = 'ADMINISTRATOR';
          }
        });
        // Now fetch status for each file
      },
      error: (error) => {
        console.error('Error fetching users:', error);
        this.errorMessage = 'Failed to fetch users. Please try again.';
      }
    });
  }

  deleteUser(user_id: string) {
    const token = this.cookieService.get('jwtToken');  // Assuming you have a JWT token for authentication
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`  // Send token if authentication is required
    });

    this.http.delete(`http://localhost:8080/api/users/${user_id}`, { headers }).subscribe({
      next: () => {
        console.log('User deleted successfully');
        this.fetchUsers();
        // You can refresh the user list or give feedback here
      },
      error: (error) => {
        console.error('Error deleting user:', error);
        // Handle the error or display an error message
      }
    });
  }
}
