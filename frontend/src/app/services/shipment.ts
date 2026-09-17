import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environment';

@Injectable({
  providedIn: 'root'
})
export class ShipmentService {

  private apiUrl =
    `${environment.apiUrl}/bff/v1/shipments`;

  constructor(
    private http: HttpClient
  ) {}

  getShipments(): Observable<any[]> {
    return this.http.get<any[]>(
      this.apiUrl
    );
  }

  createShipment(shipment: any): Observable<any> {
    return this.http.post<any>(
      this.apiUrl,
      shipment
    );
  }

  changeStatus(
    id: number,
    estado: string
  ): Observable<any> {

    return this.http.put<any>(
      `${this.apiUrl}/${id}/estado`,
      {
        estado: estado
      }
    );
  }
}