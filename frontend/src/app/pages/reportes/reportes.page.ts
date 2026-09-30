import {
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { ReportService } from '../../services/report';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reportes.page.html',
  styleUrl: './reportes.page.css'
})
export class ReportesPage implements OnInit, OnDestroy {

  kpis: any = {
    totalEventos: 0,
    creados: 0,
    aceptados: 0,
    enBodega: 0,
    enRuta: 0,
    entregados: 0,
    cancelados: 0
  };

  topServices: any = {
    servicios: {}
  };

  errorMessage = '';

  private intervaloActualizacion: any;

  constructor(
    private reportService: ReportService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    this.cargarReportes();

    this.intervaloActualizacion = setInterval(() => {
      this.cargarReportes();
    }, 2000);
  }

  ngOnDestroy(): void {

    if (this.intervaloActualizacion) {
      clearInterval(this.intervaloActualizacion);
    }
  }

  cargarReportes(): void {

    this.errorMessage = '';

    this.reportService
      .getKpis()
      .subscribe({

        next: (data) => {

          console.log(
            'KPIs recibidos:',
            data
          );

          this.kpis = data;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error obteniendo KPIs:',
            error
          );

          this.errorMessage =
            'No se pudieron cargar los KPIs.';

          this.cdr.detectChanges();
        }

      });

    this.reportService
      .getTopServices()
      .subscribe({

        next: (data) => {

          console.log(
            'Top servicios recibidos:',
            data
          );

          this.topServices = data;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error obteniendo top servicios:',
            error
          );

          this.errorMessage =
            'No se pudieron cargar los servicios.';

          this.cdr.detectChanges();
        }

      });

  }

  get serviciosArray():
    { nombre: string; cantidad: number }[] {

    if (!this.topServices?.servicios) {
      return [];
    }

    return Object
      .entries(this.topServices.servicios)
      .map(
        ([nombre, cantidad]) => ({
          nombre,
          cantidad: Number(cantidad)
        })
      );
  }
}