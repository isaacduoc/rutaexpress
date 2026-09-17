import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';

import { LoginComponent } from './pages/login/login';
import { AuditoriaPage } from './pages/auditoria/auditoria.page';
import { CatalogoPage } from './pages/catalogo/catalogo.page';
import { DashboardComponent } from './pages/dashboard/dashboard';
import { ReportesPage } from './pages/reportes/reportes.page';
import { ShipmentsComponent } from './pages/shipments/shipments';

export const routes: Routes = [

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  {
    path: 'login',
    component: LoginComponent
  },

  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [MsalGuard]
  },

  {
    path: 'shipments',
    component: ShipmentsComponent,
    canActivate: [MsalGuard]
  },

  {
    path: 'catalogo',
    component: CatalogoPage,
    canActivate: [MsalGuard]
  },

  {
    path: 'reportes',
    component: ReportesPage,
    canActivate: [MsalGuard]
  },

  {
    path: 'auditoria',
    component: AuditoriaPage,
    canActivate: [MsalGuard]
  },

  {
    path: '**',
    redirectTo: 'login'
  }

];