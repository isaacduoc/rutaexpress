import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  RouterOutlet,
  RouterLink,
  RouterLinkActive,
  Router
} from '@angular/router';

import { MsalService } from '@azure/msal-angular';
import { AccountInfo } from '@azure/msal-browser';

import { environment } from '../environment';

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
  rolesCargados = false;

  username = '';

  /*
   * Roles obtenidos desde el access token
   * emitido para RutaExpress.
   */
  userRoles: string[] = [];

  private msalService =
    inject(MsalService);

  private router =
    inject(Router);

  private cdr =
    inject(ChangeDetectorRef);


  ngOnInit(): void {

    this.msalService
      .handleRedirectObservable()
      .subscribe({

        next: (result) => {

          /*
           * Microsoft acaba de devolver
           * una cuenta después del login.
           */
          if (result?.account) {

            this.msalService.instance
              .setActiveAccount(
                result.account
              );

            this.isLoggedIn = true;

            this.username =
              result.account.username;

            /*
             * Antes de cargar roles,
             * ocultamos temporalmente
             * las opciones dependientes de rol.
             */
            this.rolesCargados = false;

            this.cargarRoles(
              result.account
            );

            this.router.navigate([
              '/dashboard'
            ]);

            return;
          }


          /*
           * Si ya existía una sesión guardada.
           */
          let account =
            this.msalService.instance
              .getActiveAccount();

          if (!account) {

            const accounts =
              this.msalService.instance
                .getAllAccounts();

            if (accounts.length > 0) {

              account =
                accounts[0];

              this.msalService.instance
                .setActiveAccount(
                  account
                );
            }
          }


          this.isLoggedIn =
            !!account;

          this.username =
            account?.username ?? '';


          /*
           * Si existe sesión,
           * obtenemos los roles.
           */
          if (account) {

            this.rolesCargados = false;

            this.cargarRoles(
              account
            );

          } else {

            /*
             * No hay sesión.
             */
            this.userRoles = [];
            this.rolesCargados = false;
          }


          /*
           * Si está autenticado pero
           * quedó en /login.
           */
          if (
            account &&
            this.router.url === '/login'
          ) {

            this.router.navigate([
              '/dashboard'
            ]);
          }

        },


        error: (error) => {

          console.error(
            'Error procesando MSAL:',
            error
          );

          this.isLoggedIn = false;
          this.rolesCargados = false;
          this.userRoles = [];

        }

      });

  }


  /*
   * ==================================================
   * CARGAR ROLES DESDE ACCESS TOKEN
   * ==================================================
   */

  private cargarRoles(
    account: AccountInfo
  ): void {

    this.msalService
      .acquireTokenSilent({

        account,

        scopes: [
          environment.rutaExpressScope
        ],

        redirectUri:
          environment.azure.silentRedirectUri

      })
      .subscribe({

        next: (result) => {

          const claims =
            this.decodeJwtPayload(
              result.accessToken
            );

          this.userRoles =
            claims?.roles ?? [];

          this.rolesCargados = true;

          console.log(
            'Roles Navbar:',
            this.userRoles
          );

          /*
           * Fuerza a Angular a actualizar
           * inmediatamente el navbar.
           */
          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(
            'Error obteniendo roles del Navbar:',
            error
          );

          this.userRoles = [];

          this.rolesCargados = true;

          this.cdr.detectChanges();

        }

      });
  }


  /*
   * ==================================================
   * PERMISOS DEL MENÚ
   * ==================================================
   */


  /*
   * Permite saber si el usuario
   * actual tiene rol Cliente.
   */
  esCliente(): boolean {

    return this.userRoles.includes(
      'Cliente'
    );
  }


  /*
   * Admin, Despachador y Cliente
   * pueden ingresar a Envíos.
   */
  puedeVerEnvios(): boolean {

    return (
      this.userRoles.includes('Admin') ||
      this.userRoles.includes('Despachador') ||
      this.userRoles.includes('Cliente')
    );
  }


  /*
   * Admin y Despachador
   * pueden ingresar al Catálogo.
   */
  puedeVerCatalogo(): boolean {

    return (
      this.userRoles.includes('Admin') ||
      this.userRoles.includes('Despachador')
    );
  }


  /*
   * Solo Admin puede
   * ingresar a Reportes.
   */
  puedeVerReportes(): boolean {

    return this.userRoles.includes(
      'Admin'
    );
  }


  /*
   * Admin y Auditor pueden
   * ingresar a Auditoría.
   */
  puedeVerAuditoria(): boolean {

    return (
      this.userRoles.includes('Admin') ||
      this.userRoles.includes('Auditor')
    );
  }


  /*
   * ==================================================
   * DECODIFICAR JWT
   * ==================================================
   */

  private decodeJwtPayload(
    token: string
  ): any {

    try {

      const payload =
        token.split('.')[1];

      if (!payload) {
        return {};
      }

      const base64 =
        payload
          .replace(/-/g, '+')
          .replace(/_/g, '/');

      const padded =
        base64.padEnd(
          base64.length +
          (4 - base64.length % 4) % 4,
          '='
        );

      return JSON.parse(
        atob(padded)
      );

    } catch (error) {

      console.error(
        'No se pudo decodificar el access token:',
        error
      );

      return {};
    }
  }


  /*
   * ==================================================
   * LOGOUT
   * ==================================================
   */

  logout(): void {

    const account =
      this.msalService.instance
        .getActiveAccount();

    this.userRoles = [];
    this.rolesCargados = false;
    this.isLoggedIn = false;

    this.msalService.logoutRedirect({

      account:
        account ?? undefined,

      postLogoutRedirectUri:
        environment.azure.redirectUri

    });

  }

}