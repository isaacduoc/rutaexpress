import { Component, OnInit, inject } from '@angular/core';
import {
  RouterOutlet,
  RouterLink,
  RouterLinkActive,
  Router
} from '@angular/router';

import { MsalService } from '@azure/msal-angular';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit {

  title = 'frontend-rutaexpress';

  isLoggedIn = false;
  username = '';

  private msalService = inject(MsalService);
  private router = inject(Router);

  ngOnInit(): void {

    this.msalService
      .handleRedirectObservable()
      .subscribe({

        next: (result) => {

          // Si Microsoft acaba de devolvernos una cuenta
          if (result?.account) {

            this.msalService.instance
              .setActiveAccount(result.account);

            this.isLoggedIn = true;
            this.username = result.account.username;

            this.router.navigate(['/dashboard']);

            return;
          }

          // Si ya existía una sesión guardada
          let account =
            this.msalService.instance.getActiveAccount();

          if (!account) {

            const accounts =
              this.msalService.instance.getAllAccounts();

            if (accounts.length > 0) {

              account = accounts[0];

              this.msalService.instance
                .setActiveAccount(account);

            }
          }

          this.isLoggedIn = !!account;
          this.username = account?.username ?? '';

          // Si ya está autenticado pero quedó en /login
          if (
            account &&
            this.router.url === '/login'
          ) {
            this.router.navigate(['/dashboard']);
          }

        },

        error: (error) => {

          console.error(
            'Error procesando MSAL:',
            error
          );

        }

      });

  }

  logout(): void {

    const account =
      this.msalService.instance.getActiveAccount();

    this.msalService.logoutRedirect({
      account: account ?? undefined,
      postLogoutRedirectUri:
        'http://localhost:4200/login'
    });

  }

}