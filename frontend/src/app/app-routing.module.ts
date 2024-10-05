import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {ProductPageComponent} from "./product-page/product-page.component";
import {CreateUserComponent} from "./create-user/create-user.component";
import {AuthService} from "./auth.service";

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'product', component: ProductPageComponent, canActivate: [AuthService] },
  { path: 'manage', component: CreateUserComponent, canActivate: [AuthService] },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
