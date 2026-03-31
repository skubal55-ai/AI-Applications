import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', loadChildren: () => import('./components/home/home.module').then((m) => m.HomeModule) },
  { path: 'login', loadChildren: () => import('./components/auth/auth.module').then((m) => m.AuthModule) },
  { path: 'register', redirectTo: '/login', pathMatch: 'full' },
  {
    path: 'services',
    canActivate: [AuthGuard],
    loadChildren: () => import('./components/services/services.module').then((m) => m.ServicesModule)
  },
  {
    path: 'staff',
    canActivate: [AuthGuard],
    loadChildren: () => import('./components/staff/staff.module').then((m) => m.StaffModule)
  },
  {
    path: 'book',
    canActivate: [AuthGuard],
    loadChildren: () => import('./components/booking/booking.module').then((m) => m.BookingModule)
  },
  {
    path: 'appointments',
    canActivate: [AuthGuard],
    loadChildren: () => import('./components/appointments/appointments.module').then((m) => m.AppointmentsModule)
  },
  {
    path: 'subscription',
    canActivate: [AuthGuard],
    loadChildren: () => import('./components/subscription/subscription.module').then((m) => m.SubscriptionModule)
  },
  { path: '**', redirectTo: '/home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
