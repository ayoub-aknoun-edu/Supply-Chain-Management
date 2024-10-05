import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';

import {HttpClient, HttpErrorResponse, HttpHeaders, HttpResponse} from "@angular/common/http";
import {CookieService} from "ngx-cookie-service";


export class ProductPageComponent implements OnInit  {
  username: string = this.cookieService.get('username');
  role: string = this.cookieService.get('role');
  product_filter: string = "received"
  privateKey: string = '';
  publicKey: string = '';
  receiverName: string = '';
  selectedFile!: File;
  users: any[] = [];
  files: any[] = [];
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(private http: HttpClient, private cookieService: CookieService, private elRef: ElementRef) {}

  @ViewChild('receiverModal', { static: false }) modal: ElementRef | undefined;

  closeModal() {
    // @ts-ignore
    const modalElement = document.getElementById('receiverModal');

    // Manually remove the 'show' class and update attributes
    modalElement!.classList.remove('show');
    modalElement!.style.display = 'none'; // Hide the modal by setting display to 'none'
    modalElement!.setAttribute('aria-hidden', 'true'); // Set aria-hidden attribute to true
    modalElement!.removeAttribute('aria-modal'); // Remove aria-modal attribute
    modalElement!.classList.add('fade'); // Ensure fade class is still present

    // Remove backdrop manually if it's present (Bootstrap adds it when modal is shown)
    const backdrop = document.querySelector('.modal-backdrop');
    if (backdrop) {
      // @ts-ignore
      backdrop.parentNode.removeChild(backdrop);
    }
  }

  ngOnInit() {
    this.fetchFiles();
    this.fetchUsers();
  }

  onLogout() {
    // Clear JWT token from cookies
    this.cookieService.delete('jwtToken');
    this.cookieService.delete('username');
  }

  fetchUsers() {
    const token = this.cookieService.get('jwtToken');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'accept': '*/*'
    });

    this.http.get<any[]>('http://localhost:8080/api/users/exceptme', { headers }).subscribe({
      next: (response) => {
        this.users = response;
      },
      error: (error) => {
        console.error('Error fetching users:', error);
      }
    });
  }

  fetchFiles() {
    let link = ''
    if (this.product_filter === "received"){
      link = 'http://localhost:8080/api/products/receiver';
    } else if (this.product_filter === "sent"){
      link = 'http://localhost:8080/api/products/sender';
    } else {
      link = 'http://localhost:8080/api/products';
    }


    const token = this.cookieService.get('jwtToken');  // Get JWT from cookies

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,  // Include JWT in headers
      'accept': '*/*'
    });

    // Send GET request to fetch files
    this.http.get<any[]>(link, { headers }).subscribe({
      next: (response) => {
        this.files = response;  // Update the file list with the response
        // Now fetch status for each file
      },
      error: (error) => {
        console.error('Error fetching files:', error);
        this.errorMessage = 'Failed to fetch files. Please try again.';
      }
    });
  }


  triggerFileInput() {
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    fileInput.click();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];

    }
  }
  uploadFile(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('receiver', this.receiverName);
    const token = this.cookieService.get('jwtToken');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'accept': '*/*'
    });

    this.http.post('http://localhost:8080/api/products', formData, { headers }).subscribe({
      next: (response) => {
        console.log('File uploaded successfully:', response);
        this.fetchFiles();
        this.successMessage = 'File uploaded successfully!';
        this.errorMessage = null;
        this.receiverName = '';
        // Clear the file input
        this.selectedFile = new File([], '');
        // Select the file input element
        const fileInput = document.getElementById('fileInput') as HTMLInputElement;
        // Reset the file input element
        fileInput.value = '';
        // Hide the modal
        const modal = document.getElementById('receiverModal');
        this.closeModal();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (error) => {
        console.error('Upload failed', error);
        this.errorMessage = 'File upload failed. Please try again.';
        setTimeout(() => this.errorMessage = null, 3000);
      }
    });
  }


  verifyProduct(product_id: string, product_signer: string) {
    const token = this.cookieService.get('jwtToken');  // Get JWT from cookies

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,  // Include JWT in headers
      'accept': '*/*'
    });

    this.http.post<any[]>(`http://localhost:8080/api/products/${product_id}/verify/${product_signer}`, {},{ headers }).subscribe({
      next: (response) => {
       this.fetchFiles()
        // Now fetch status for each file
      },
      error: (error) => {
        console.error('Error verifying product:', error);
        this.errorMessage = 'Failed to verify productt. Please try again.';
      }
    });
  }


  downloadProduct(productId: string, fileName: string) {
    const token = this.cookieService.get('jwtToken');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'accept': '*/*'
    });

    this.http.get(`http://localhost:8080/api/products/download/${productId}`, {
      headers: headers,
      responseType: 'blob'
    }).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Download failed:', error);
        this.errorMessage = 'Download failed. Please try again.';
        setTimeout(() => this.errorMessage = null, 3000);
      }
    });
  }

  fetchFilesChange(filter: string) {
    this.product_filter = filter;
    this.fetchFiles();
  }

  getSecretKeys() {
    const token = this.cookieService.get('jwtToken');  // Get JWT from cookies

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,  // Include JWT in headers
      'accept': '*/*'
    });

    this.http.get<any>("http://localhost:8080/api/users/secretkey", { headers }).subscribe({
      next: (response) => {
        this.privateKey = response.privateKey;  // Set the private key
        this.publicKey = response.publicKey;    // Set the public key
      },
      error: (error) => {
        console.error('Error fetching secret keys:', error);
        this.errorMessage = 'Failed to fetch keys. Please try again.';
      }
    });
  }
}
