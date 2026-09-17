import {
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';

import { AuditService } from '../../services/audit';

@Component({
  imports: [CommonModule],
  selector: 'app-auditoria',
  styleUrl: './auditoria.page.css',
  templateUrl: './auditoria.page.html',
})
export class AuditoriaPage implements OnInit, OnDestroy {

  eventos: any[] = [];

  loading = true;
  errorMessage = '';

  private intervaloActualizacion: any;

  constructor(
    private auditService: AuditService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    this.cargarEventos();

    this.intervaloActualizacion = setInterval(() => {
      this.cargarEventos();
    }, 2000);
  }

  ngOnDestroy(): void {

    if (this.intervaloActualizacion) {
      clearInterval(this.intervaloActualizacion);
    }
  }

  cargarEventos(): void {

    this.auditService
      .getEventos()
      .subscribe({

        next: (data) => {

          this.eventos = data;

          this.loading = false;
          this.errorMessage = '';

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error cargando auditoría:',
            error
          );

          this.errorMessage =
            'No se pudieron cargar los eventos de auditoría.';

          this.loading = false;

          this.cdr.detectChanges();
        }

      });

  }

}