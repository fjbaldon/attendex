import {Routes} from '@angular/router';
import {MainLayout} from './layout/main-layout/main-layout';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout,
    children: [
      {path: '', redirectTo: 'dashboard', pathMatch: 'full'},
    ],
  },

  {path: '**', redirectTo: ''},
];
