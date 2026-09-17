import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuditService {

  private readonly baseUrl =
    'http://localhost:8081/bff/v1/audit';

  constructor(
    private http: HttpClient
  ) {}

  getEventos(): Observable<any[]> {
    return this.http.get<any[]>(
      this.baseUrl
    );
  }

  getEventosPorEnvio(
    shipmentId: number
  ): Observable<any[]> {

    return this.http.get<any[]>(
      `${this.baseUrl}/shipment/${shipmentId}`
    );
  }

  getEventosPorTracking(
    codigoSeguimiento: string
  ): Observable<any[]> {

    return this.http.get<any[]>(
      `${this.baseUrl}/tracking/${codigoSeguimiento}`
    );
  }

  getEntregadosHoy(): Observable<{
    entregadosHoy: number
  }> {

    return this.http.get<{
      entregadosHoy: number
    }>(
      `${this.baseUrl}/entregados-hoy`
    );
  }
}