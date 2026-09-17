import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ShipmentService } from '../../services/shipment';
import { CatalogService } from '../../services/catalog';

@Component({
  selector: 'app-shipments',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './shipments.html',
  styleUrl: './shipments.css'
})
export class ShipmentsComponent implements OnInit {

  shipments: any[] = [];
  services: any[] = [];

  loading = true;
  errorMessage = '';
  successMessage = '';

  creandoEnvio = false;
  actualizandoEstadoId: number | null = null;

  nuevoEnvio = {
    correoRemitente: '',
    nombreDestinatario: '',
    correoDestinatario: '',
    direccionOrigen: '',
    direccionDestino: '',
    servicio: ''
  };

  constructor(
    private shipmentService: ShipmentService,
    private catalogService: CatalogService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarEnvios();
    this.cargarServicios();
  }

  cargarEnvios(): void {

    this.loading = true;

    this.shipmentService
      .getShipments()
      .subscribe({

        next: (data) => {

          console.log(
            'Envíos recibidos:',
            data
          );

          this.shipments = data;
          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error cargando los envíos:',
            error
          );

          this.errorMessage =
            'No se pudieron cargar los envíos.';

          this.loading = false;

          this.cdr.detectChanges();
        }

      });

  }

  cargarServicios(): void {

    this.catalogService
      .getServices()
      .subscribe({

        next: (data) => {

          console.log(
            'Servicios recibidos:',
            data
          );

          this.services = data;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error cargando servicios:',
            error
          );

          this.cdr.detectChanges();
        }

      });

  }

  crearEnvio(): void {

    this.errorMessage = '';
    this.successMessage = '';

    if (
      !this.nuevoEnvio.correoRemitente ||
      !this.nuevoEnvio.nombreDestinatario ||
      !this.nuevoEnvio.correoDestinatario ||
      !this.nuevoEnvio.direccionOrigen ||
      !this.nuevoEnvio.direccionDestino ||
      !this.nuevoEnvio.servicio
    ) {

      this.errorMessage =
        'Debes completar todos los campos antes de crear el envío.';

      this.cdr.detectChanges();

      return;
    }

    if (this.creandoEnvio) {
      return;
    }

    this.creandoEnvio = true;

    this.shipmentService
      .createShipment(this.nuevoEnvio)
      .subscribe({

        next: (response) => {

          console.log(
            'Envío creado:',
            response
          );

          this.successMessage =
            'Envío creado correctamente.';

          this.nuevoEnvio = {
            correoRemitente: '',
            nombreDestinatario: '',
            correoDestinatario: '',
            direccionOrigen: '',
            direccionDestino: '',
            servicio: ''
          };

          this.creandoEnvio = false;

          this.cargarEnvios();

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error creando envío:',
            error
          );

          this.errorMessage =
            'No se pudo crear el envío.';

          this.creandoEnvio = false;

          this.cdr.detectChanges();
        }

      });

  }

  cambiarEstado(
    id: number,
    estado: string
  ): void {

    if (this.actualizandoEstadoId !== null) {
      return;
    }

    this.actualizandoEstadoId = id;

    this.errorMessage = '';
    this.successMessage = '';

    this.shipmentService
      .changeStatus(id, estado)
      .subscribe({

        next: (response) => {

          console.log(
            'Estado actualizado:',
            response
          );

          this.successMessage =
            `Estado actualizado a ${estado}.`;

          this.actualizandoEstadoId = null;

          this.cargarEnvios();
          this.cargarServicios();

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error cambiando estado:',
            error
          );

          this.errorMessage =
            'No se pudo cambiar el estado del envío.';

          this.actualizandoEstadoId = null;

          this.cdr.detectChanges();
        }

      });

  }

}