import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environment';

@Injectable({
  providedIn: 'root'
})
export class CatalogService {

  private apiUrl =
    `${environment.apiUrl}/bff/v1/catalog/services`;

  constructor(
    private http: HttpClient
  ) {}

  getServices(): Observable<any[]> {
    return this.http.get<any[]>(
      this.apiUrl
    );
  }

  createService(servicio: {
    nombre: string;
    tarifaBase: number;
    capacidadDisponible: number;
  }): Observable<any> {

    return this.http.post<any>(
      this.apiUrl,
      servicio
    );
  }
}