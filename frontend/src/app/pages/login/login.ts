import { Component } from '@angular/core';
import { MsalService } from '@azure/msal-angular';
import { environment } from '../../../environment';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {

  constructor(
    private authService: MsalService
  ) {}

  login(): void {

    this.authService.loginRedirect({
      scopes: [
        environment.rutaExpressScope
      ]
    });

  }

}