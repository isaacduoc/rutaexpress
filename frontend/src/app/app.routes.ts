import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';

import { roleGuard } from './guards/role.guard';

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
    canActivate: [
      MsalGuard
    ]
  },

  {
    path: 'shipments',
    component: ShipmentsComponent,
    canActivate: [
      MsalGuard,
      roleGuard
    ],
    data: {
      roles: [
        'Admin',
        'Despachador',
        'Cliente'
      ]
    }
  },

  {
    path: 'catalogo',
    component: CatalogoPage,
    canActivate: [
      MsalGuard,
      roleGuard
    ],
    data: {
      roles: [
        'Admin',
        'Despachador'
      ]
    }
  },

  {
    path: 'reportes',
    component: ReportesPage,
    canActivate: [
      MsalGuard,
      roleGuard
    ],
    data: {
      roles: [
        'Admin'
      ]
    }
  },

  {
    path: 'auditoria',
    component: AuditoriaPage,
    canActivate: [
      MsalGuard,
      roleGuard
    ],
    data: {
      roles: [
        'Admin',
        'Auditor'
      ]
    }
  },

  {
    path: '**',
    redirectTo: 'login'
  }

];