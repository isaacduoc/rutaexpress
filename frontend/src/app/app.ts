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

  /*
   * Scopes obtenidos desde el claim "scp"
   * del access token de RutaExpress.
   */
  userScopes: string[] = [];

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
             * Antes de cargar roles y scopes,
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
           * obtenemos roles y scopes.
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
            this.userScopes = [];
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
          this.userScopes = [];

        }

      });

  }


  /*
   * ==================================================
   * CARGAR ROLES Y SCOPES DESDE ACCESS TOKEN
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

          /*
           * Roles asignados en Microsoft Entra.
           */
          this.userRoles =
            Array.isArray(claims?.roles)
              ? claims.roles
              : [];


          /*
           * Los scopes delegados normalmente
           * vienen en el claim "scp" como
           * una cadena separada por espacios.
           *
           * Ejemplo:
           * "access_as_user otro_scope"
           */
          const scopeClaim =
            claims?.scp;

          this.userScopes =
            typeof scopeClaim === 'string'
              ? scopeClaim
                  .split(' ')
                  .filter(Boolean)
              : [];


          this.rolesCargados = true;


          console.log(
            'Roles Navbar:',
            this.userRoles
          );

          console.log(
            'Scopes Navbar:',
            this.userScopes
          );


          /*
           * Fuerza a Angular a actualizar
           * inmediatamente el navbar.
           */
          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(
            'Error obteniendo roles y scopes del Navbar:',
            error
          );

          this.userRoles = [];
          this.userScopes = [];

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
    this.userScopes = [];
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