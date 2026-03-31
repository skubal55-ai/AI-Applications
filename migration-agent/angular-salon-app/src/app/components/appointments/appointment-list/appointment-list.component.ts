import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, catchError, finalize } from 'rxjs/operators';
import { EMPTY } from 'rxjs';
import { AppointmentService } from '../../../services/appointment.service';
import { Appointment, AppointmentStatus } from '../../../models/appointment.model';

@Component({
  selector: 'app-appointment-list',
  templateUrl: './appointment-list.component.html',
  styleUrls: ['./appointment-list.component.scss']
})
export class AppointmentListComponent implements OnInit, OnDestroy {
  appointments: Appointment[] = [];
  filteredAppointments: Appointment[] = [];
  isLoading = false;
  errorMessage = '';
  activeFilter: AppointmentStatus | 'ALL' = 'ALL';

  filters: Array<{ label: string; value: AppointmentStatus | 'ALL' }> = [
    { label: 'All', value: 'ALL' },
    { label: 'Pending', value: 'PENDING' },
    { label: 'Confirmed', value: 'CONFIRMED' },
    { label: 'Completed', value: 'COMPLETED' },
    { label: 'Cancelled', value: 'CANCELLED' }
  ];

  private destroy$ = new Subject<void>();

  constructor(private appointmentService: AppointmentService) {}

  ngOnInit(): void {
    this.loadAppointments();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAppointments(): void {
    this.isLoading = true;
    this.appointmentService.getMyAppointments()
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          this.errorMessage = err.error?.message || 'Failed to load appointments';
          return EMPTY;
        }),
        finalize(() => (this.isLoading = false))
      )
      .subscribe((appointments) => {
        this.appointments = appointments;
        this.applyFilter();
      });
  }

  setFilter(filter: AppointmentStatus | 'ALL'): void {
    this.activeFilter = filter;
    this.applyFilter();
  }

  private applyFilter(): void {
    this.filteredAppointments =
      this.activeFilter === 'ALL'
        ? this.appointments
        : this.appointments.filter((a) => a.status === this.activeFilter);
  }

  cancelAppointment(id: number): void {
    if (!confirm('Are you sure you want to cancel this appointment?')) return;
    this.appointmentService.cancelAppointment(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updated) => {
          this.appointments = this.appointments.map((a) =>
            a.id === id ? updated : a
          );
          this.applyFilter();
        },
        error: (err) => {
          alert(err.error?.message || 'Failed to cancel appointment');
        }
      });
  }

  getStatusClass(status: AppointmentStatus): string {
    const map: Record<AppointmentStatus, string> = {
      PENDING: 'status-pending',
      CONFIRMED: 'status-confirmed',
      COMPLETED: 'status-completed',
      CANCELLED: 'status-cancelled'
    };
    return map[status];
  }

  formatDate(dateStr: string): string {
    return new Intl.DateTimeFormat('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    }).format(new Date(dateStr));
  }

  trackById(index: number, appointment: Appointment): number {
    return appointment.id;
  }
}
