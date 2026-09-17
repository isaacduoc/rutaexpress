import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environment';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  private apiUrl = `${environment.apiUrl}/bff/v1/report`;

  constructor(private http: HttpClient) {}

  getKpis(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/kpis`);
  }

  getTopServices(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/top-services`);
  }
}