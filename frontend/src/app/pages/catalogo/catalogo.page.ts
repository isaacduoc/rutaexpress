import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CatalogService } from '../../services/catalog';

@Component({
  selector: 'app-catalogo',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  styleUrl: './catalogo.page.css',
  templateUrl: './catalogo.page.html',
})
export class CatalogoPage implements OnInit {

  servicios: any[] = [];

  loading = true;
  errorMessage = '';
  successMessage = '';

  mostrarFormulario = false;
  creandoServicio = false;

  nuevoServicio = {
    nombre: '',
    tarifaBase: 0,
    capacidadDisponible: 0
  };

  constructor(
    private catalogService: CatalogService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarServicios();
  }

  cargarServicios(): void {

    this.loading = true;
    this.errorMessage = '';

    this.catalogService
      .getServices()
      .subscribe({

        next: (data) => {

          this.servicios = data;
          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error cargando catálogo:',
            error
          );

          this.errorMessage =
            'No se pudieron cargar los servicios del catálogo.';

          this.loading = false;

          this.cdr.detectChanges();
        }

      });
  }

  alternarFormulario(): void {

    this.mostrarFormulario =
      !this.mostrarFormulario;

    this.errorMessage = '';
    this.successMessage = '';

    this.cdr.detectChanges();
  }

  crearServicio(): void {

    this.errorMessage = '';
    this.successMessage = '';

    if (
      !this.nuevoServicio.nombre.trim() ||
      this.nuevoServicio.tarifaBase <= 0 ||
      this.nuevoServicio.capacidadDisponible <= 0
    ) {

      this.errorMessage =
        'Debes completar todos los campos con valores válidos.';

      this.cdr.detectChanges();

      return;
    }

    if (this.creandoServicio) {
      return;
    }

    this.creandoServicio = true;

    this.catalogService
      .createService(this.nuevoServicio)
      .subscribe({

        next: (response) => {

          console.log(
            'Servicio creado:',
            response
          );

          this.successMessage =
            'Servicio creado correctamente.';

          /*
           * Agregamos directamente el servicio devuelto
           * por el backend a la tabla.
           *
           * Así evitamos volver a hacer otro GET
           * de todo el catálogo.
           */
          this.servicios = [
            ...this.servicios,
            response
          ];

          this.nuevoServicio = {
            nombre: '',
            tarifaBase: 0,
            capacidadDisponible: 0
          };

          this.creandoServicio = false;
          this.mostrarFormulario = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error creando servicio:',
            error
          );

          this.errorMessage =
            'No se pudo crear el servicio.';

          this.creandoServicio = false;

          this.cdr.detectChanges();
        }

      });
  }
}