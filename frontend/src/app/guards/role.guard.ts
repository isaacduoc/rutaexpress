import { inject } from '@angular/core';

import {
  CanActivateFn,
  Router
} from '@angular/router';

import {
  MsalBroadcastService,
  MsalService
} from '@azure/msal-angular';

import {
  InteractionStatus
} from '@azure/msal-browser';

import {
  catchError,
  filter,
  map,
  of,
  switchMap,
  take
} from 'rxjs';

import { environment } from '../../environment';


function decodeJwtPayload(token: string): any {

  try {

    const payload = token.split('.')[1];

    if (!payload) {
      return {};
    }

    const base64 = payload
      .replace(/-/g, '+')
      .replace(/_/g, '/');

    const padded = base64.padEnd(
      base64.length + (4 - base64.length % 4) % 4,
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


export const roleGuard: CanActivateFn = (route) => {

  const msalService = inject(MsalService);

  const msalBroadcastService =
    inject(MsalBroadcastService);

  const router = inject(Router);

  const rolesPermitidos =
    route.data?.['roles'] as string[] ?? [];

  /*
   * Esperamos hasta que MSAL haya terminado
   * cualquier login, redirect o restauración
   * de sesión.
   */
  return msalBroadcastService.inProgress$
    .pipe(

      filter(
        status =>
          status === InteractionStatus.None
      ),

      take(1),

      switchMap(() => {

        const accounts =
          msalService.instance.getAllAccounts();

        let account =
          msalService.instance.getActiveAccount();

        /*
         * Después de recargar el navegador,
         * puede existir la cuenta en caché pero
         * no estar marcada todavía como activa.
         */
        if (!account && accounts.length > 0) {

          account = accounts[0];

          msalService.instance.setActiveAccount(
            account
          );
        }

        if (!account) {

          console.warn(
            'No existe una sesión activa.'
          );

          return of(
            router.createUrlTree([
              '/login'
            ])
          );
        }

        return msalService.acquireTokenSilent({

          account,

          scopes: [
            environment.rutaExpressScope
          ],

          redirectUri:
            environment.azure.silentRedirectUri

        }).pipe(

          map(result => {

            const claims =
              decodeJwtPayload(
                result.accessToken
              );

            const rolesUsuario: string[] =
              claims?.roles ?? [];

            console.log(
              'Roles del usuario:',
              rolesUsuario
            );

            console.log(
              'Roles requeridos:',
              rolesPermitidos
            );

            const autorizado =
              rolesPermitidos.some(
                rol =>
                  rolesUsuario.includes(rol)
              );

            if (autorizado) {

              console.log(
                'Acceso autorizado por rol.'
              );

              return true;
            }

            console.warn(
              'Acceso denegado por rol.'
            );

            return router.createUrlTree([
              '/dashboard'
            ]);

          }),

          catchError(error => {

            console.error(
              'No se pudo obtener el access token:',
              error
            );

            return of(
              router.createUrlTree([
                '/login'
              ])
            );

          })

        );

      })

    );
};