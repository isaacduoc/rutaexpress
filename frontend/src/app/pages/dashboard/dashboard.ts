import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { MsalService } from '@azure/msal-angular';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ShipmentService } from '../../services/shipment';
import { AuditService } from '../../services/audit';

import { environment } from '../../../environment';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    RouterLink,
    CommonModule,
    FormsModule
  ],
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {

  userEmail = '';
  userRoles: string[] = [];

  enviosActivos = 0;
  entregadosHoy = 0;

  /*
   * Lista de envíos pertenecientes
   * al Cliente autenticado.
   */
  enviosCliente: any[] = [];

  /*
   * Buscador de seguimiento.
   */
  codigoBusqueda = '';

  envioSeleccionado: any = null;

  mensajeBusqueda = '';

  cargandoEnvios = false;
  buscandoEnvio = false;

  /*
   * Flujo normal de un envío.
   */
  estadosEnvio: string[] = [
    'CREADO',
    'ACEPTADO',
    'EN_BODEGA',
    'EN_RUTA',
    'ENTREGADO'
  ];

  private authService =
    inject(MsalService);

  constructor(
    private shipmentService: ShipmentService,
    private auditService: AuditService,
    private cdr: ChangeDetectorRef
  ) {}


  ngOnInit(): void {

    this.cargarUsuario();
  }


  /*
   * ==================================================
   * USUARIO Y ROLES
   * ==================================================
   */

  cargarUsuario(): void {

    const instance =
      this.authService.instance;

    let account =
      instance.getActiveAccount();

    if (
      !account &&
      instance.getAllAccounts().length > 0
    ) {

      account =
        instance.getAllAccounts()[0];

      instance.setActiveAccount(
        account
      );
    }


    if (!account) {

      console.warn(
        'No existe una cuenta activa.'
      );

      return;
    }


    this.userEmail =
      account.username || 'Usuario';


    this.authService
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

          const roles: string[] =
            claims?.roles ?? [];

          this.userRoles =
            roles.length > 0
              ? roles
              : ['Sin rol asignado'];

          console.log(
            'Roles Dashboard:',
            this.userRoles
          );

          this.cargarDatosSegunRol();

          this.cdr.detectChanges();
        },


        error: (error) => {

          console.error(
            'Error obteniendo access token:',
            error
          );

          this.userRoles = [
            'Sin rol asignado'
          ];

          this.cdr.detectChanges();
        }

      });
  }


  /*
   * ==================================================
   * TIPO DE USUARIO
   * ==================================================
   */

  esCliente(): boolean {

    return this.userRoles.includes(
      'Cliente'
    );
  }


  esAdmin(): boolean {

    return this.userRoles.includes(
      'Admin'
    );
  }


  esAuditor(): boolean {

    return this.userRoles.includes(
      'Auditor'
    );
  }


  /*
   * ==================================================
   * CARGAR DATOS SEGÚN ROL
   * ==================================================
   */

  cargarDatosSegunRol(): void {

    if (this.esCliente()) {

      this.cargarEnviosCliente();

      return;
    }

    this.cargarIndicadoresGenerales();
  }


  /*
   * ==================================================
   * CLIENTE
   * ==================================================
   */

  cargarEnviosCliente(): void {

    this.cargandoEnvios = true;

    this.shipmentService
      .getShipments()
      .subscribe({

        next: (data) => {

          this.enviosCliente =
            Array.isArray(data)
              ? data
              : [];

          this.enviosActivos =
            this.enviosCliente.filter(
              (envio: any) =>
                envio.estado !== 'ENTREGADO' &&
                envio.estado !== 'CANCELADO'
            ).length;


          /*
           * Recuperamos el último código
           * que el Cliente estaba siguiendo.
           */
          const codigoGuardado =
            sessionStorage.getItem(
              'rutaexpress_codigo_seguimiento'
            );


          if (codigoGuardado) {

            this.codigoBusqueda =
              codigoGuardado;

            /*
             * Buscamos el mismo envío dentro
             * de la información recién obtenida
             * desde el backend.
             *
             * Así vuelve con su estado actualizado.
             */
            this.envioSeleccionado =
              this.enviosCliente.find(
                (envio: any) =>
                  envio.codigoSeguimiento
                    ?.trim()
                    .toUpperCase() ===
                  codigoGuardado
                    .trim()
                    .toUpperCase()
              ) ?? null;


            /*
             * Si por alguna razón ya no existe
             * o no pertenece al Cliente,
             * limpiamos el código guardado.
             */
            if (!this.envioSeleccionado) {

              sessionStorage.removeItem(
                'rutaexpress_codigo_seguimiento'
              );

              this.codigoBusqueda = '';
            }

          } else {

            /*
             * Si nunca buscó un pedido,
             * no mostramos ninguno.
             */
            this.envioSeleccionado = null;
          }


          this.cargandoEnvios = false;

          console.log(
            'Envíos del Cliente:',
            this.enviosCliente
          );

          console.log(
            'Seguimiento restaurado:',
            this.envioSeleccionado
          );

          this.cdr.detectChanges();
        },


        error: (error) => {

          console.error(
            'Error cargando envíos del Cliente:',
            error
          );

          this.enviosCliente = [];

          this.enviosActivos = 0;

          this.envioSeleccionado = null;

          this.cargandoEnvios = false;

          this.cdr.detectChanges();
        }

      });
  }


  /*
   * ==================================================
   * BUSCAR ENVÍO POR CÓDIGO
   * ==================================================
   */

  buscarEnvio(): void {

    this.mensajeBusqueda = '';

    const codigo =
      this.codigoBusqueda
        .trim()
        .toUpperCase();

    if (!codigo) {

      this.envioSeleccionado = null;

      this.mensajeBusqueda =
        'Ingresa un código de seguimiento.';

      this.cdr.detectChanges();

      return;
    }


    /*
     * Volvemos a consultar el backend
     * para tener el estado más reciente.
     */
    this.buscandoEnvio = true;

    this.shipmentService
      .getShipments()
      .subscribe({

        next: (data) => {

          this.enviosCliente =
            Array.isArray(data)
              ? data
              : [];

          this.enviosActivos =
            this.enviosCliente.filter(
              (envio: any) =>
                envio.estado !== 'ENTREGADO' &&
                envio.estado !== 'CANCELADO'
            ).length;


          const encontrado =
            this.enviosCliente.find(
              (envio: any) =>
                envio.codigoSeguimiento
                  ?.trim()
                  .toUpperCase() === codigo
            );


          if (!encontrado) {

            this.envioSeleccionado = null;

            this.mensajeBusqueda =
              'No encontramos un envío con ese código asociado a tu cuenta.';

            /*
             * No dejamos guardado un código
             * que no corresponde al usuario.
             */
            sessionStorage.removeItem(
              'rutaexpress_codigo_seguimiento'
            );

          } else {

            this.envioSeleccionado =
              encontrado;

            /*
             * Dejamos el código normalizado
             * en el cuadro de búsqueda.
             */
            this.codigoBusqueda =
              encontrado.codigoSeguimiento;

            /*
             * Recordamos qué pedido estaba
             * siguiendo el Cliente.
             */
            sessionStorage.setItem(
              'rutaexpress_codigo_seguimiento',
              encontrado.codigoSeguimiento
            );

            this.mensajeBusqueda = '';

            console.log(
              'Envío encontrado:',
              encontrado
            );
          }


          this.buscandoEnvio = false;

          this.cdr.detectChanges();
        },


        error: (error) => {

          console.error(
            'Error buscando el envío:',
            error
          );

          this.envioSeleccionado = null;

          this.mensajeBusqueda =
            'No se pudo consultar el envío en este momento.';

          this.buscandoEnvio = false;

          this.cdr.detectChanges();
        }

      });
  }


  /*
   * Cerrar seguimiento manualmente.
   *
   * Aquí sí olvidamos el pedido.
   */
  limpiarBusqueda(): void {

    this.codigoBusqueda = '';

    this.envioSeleccionado = null;

    this.mensajeBusqueda = '';

    sessionStorage.removeItem(
      'rutaexpress_codigo_seguimiento'
    );

    this.cdr.detectChanges();
  }


  /*
   * ==================================================
   * PROGRESO DEL ENVÍO
   * ==================================================
   */

  porcentajeProgreso(
    estado: string
  ): number {

    if (estado === 'CANCELADO') {
      return 0;
    }

    const indice =
      this.estadosEnvio.indexOf(
        estado
      );

    if (indice < 0) {
      return 0;
    }

    return (
      (indice + 1) /
      this.estadosEnvio.length
    ) * 100;
  }


  estadoAlcanzado(
    estado: string
  ): boolean {

    if (
      !this.envioSeleccionado ||
      this.envioSeleccionado.estado === 'CANCELADO'
    ) {
      return false;
    }

    const indiceActual =
      this.estadosEnvio.indexOf(
        this.envioSeleccionado.estado
      );

    const indiceEstado =
      this.estadosEnvio.indexOf(
        estado
      );

    return (
      indiceActual >= indiceEstado
    );
  }


  /*
   * Texto amigable para Cliente.
   */
  nombreEstado(
    estado: string
  ): string {

    switch (estado) {

      case 'CREADO':
        return 'Pedido creado';

      case 'ACEPTADO':
        return 'Pedido aceptado';

      case 'EN_BODEGA':
        return 'En bodega';

      case 'EN_RUTA':
        return 'En camino';

      case 'ENTREGADO':
        return 'Entregado';

      case 'CANCELADO':
        return 'Cancelado';

      default:
        return estado;
    }
  }


  /*
   * ==================================================
   * DASHBOARD OTROS ROLES
   * ==================================================
   */

  cargarIndicadoresGenerales(): void {

    this.shipmentService
      .getShipments()
      .subscribe({

        next: (data) => {

          this.enviosActivos =
            data.filter(
              (envio: any) =>
                envio.estado !== 'ENTREGADO' &&
                envio.estado !== 'CANCELADO'
            ).length;

          this.cdr.detectChanges();
        },


        error: (error) => {

          console.error(
            'Error cargando envíos activos:',
            error
          );

          this.enviosActivos = 0;

          this.cdr.detectChanges();
        }

      });


    if (
      this.esAdmin() ||
      this.esAuditor()
    ) {

      this.auditService
        .getEntregadosHoy()
        .subscribe({

          next: (response) => {

            this.entregadosHoy =
              response.entregadosHoy;

            this.cdr.detectChanges();
          },


          error: (error) => {

            console.error(
              'Error cargando entregados hoy:',
              error
            );

            this.entregadosHoy = 0;

            this.cdr.detectChanges();
          }

        });
    }
  }


  /*
   * ==================================================
   * JWT
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

    localStorage.clear();

    /*
     * Esto también elimina automáticamente
     * el seguimiento recordado.
     */
    sessionStorage.clear();

    this.authService.logoutRedirect({

      postLogoutRedirectUri:
        window.location.origin

    });
  }
}