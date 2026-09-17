import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { MsalService } from '@azure/msal-angular';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

import { ShipmentService } from '../../services/shipment';
import { AuditService } from '../../services/audit';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {

  userEmail: string = 'el.armijo@duocuc.cl';
  userRoles: string[] = ['Usuario Estándar'];

  enviosActivos = 0;
  entregadosHoy = 0;

  private authService = inject(MsalService);

  constructor(
    private shipmentService: ShipmentService,
    private auditService: AuditService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    const instance = this.authService.instance;
    let account = instance.getActiveAccount();

    if (
      !account &&
      instance.getAllAccounts().length > 0
    ) {
      account = instance.getAllAccounts()[0];
      instance.setActiveAccount(account);
    }

    if (account) {

      this.userEmail =
        account.username ||
        'el.armijo@duocuc.cl';

      const claims =
        account.idTokenClaims as any;

      if (claims?.roles?.length) {
        this.userRoles = claims.roles;
      }
    }

    this.cargarIndicadores();
  }

  cargarIndicadores(): void {

    /*
     * Envíos activos:
     * se obtienen desde el microservicio de shipments.
     */
    this.shipmentService
      .getShipments()
      .subscribe({

        next: (data) => {

          this.enviosActivos =
            data.filter((envio: any) =>
              envio.estado !== 'ENTREGADO' &&
              envio.estado !== 'CANCELADO'
            ).length;

          console.log(
            'Envíos activos:',
            this.enviosActivos
          );

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

    /*
     * Entregados hoy:
     * se obtienen desde Auditoría usando la fecha real
     * del evento ENTREGADO.
     */
    this.auditService
      .getEntregadosHoy()
      .subscribe({

        next: (response) => {

          this.entregadosHoy =
            response.entregadosHoy;

          console.log(
            'Entregados hoy:',
            this.entregadosHoy
          );

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

  logout(): void {

    localStorage.clear();
    sessionStorage.clear();

    this.authService.logoutRedirect({
      postLogoutRedirectUri:
        window.location.origin
    });
  }
}