import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, combineLatest } from 'rxjs';
import { takeUntil, switchMap } from 'rxjs/operators';
import { AppointmentService } from '../../services/appointment.service';
import { SalonServiceApi } from '../../services/salon.service';
import { SalonService, Staff } from '../../models/service.model';

@Component({
  selector: 'app-booking',
  templateUrl: './booking.component.html',
  styleUrls: ['./booking.component.scss']
})
export class BookingComponent implements OnInit, OnDestroy {
  bookingForm!: FormGroup;
  services: SalonService[] = [];
  staffList: Staff[] = [];
  availableSlots: string[] = [];
  isLoading = false;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private appointmentService: AppointmentService,
    private salonService: SalonServiceApi
  ) {}

  ngOnInit(): void {
    this.bookingForm = this.fb.group({
      serviceId: ['', Validators.required],
      staffId: ['', Validators.required],
      appointmentDate: ['', Validators.required],
      startTime: ['', Validators.required],
      notes: ['']
    });

    this.loadInitialData();

    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      if (params['serviceId']) {
        this.bookingForm.patchValue({ serviceId: +params['serviceId'] });
      }
    });

    this.bookingForm.get('staffId')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadAvailableSlots());

    this.bookingForm.get('appointmentDate')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadAvailableSlots());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadInitialData(): void {
    this.isLoading = true;
    combineLatest([
      this.salonService.getServices(),
      this.salonService.getStaff()
    ])
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ([services, staff]) => {
          this.services = services.filter((s) => s.isActive);
          this.staffList = staff.filter((s) => s.isAvailable);
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = 'Failed to load booking data';
          this.isLoading = false;
        }
      });
  }

  loadAvailableSlots(): void {
    const { staffId, appointmentDate } = this.bookingForm.value;
    if (!staffId || !appointmentDate) return;

    this.appointmentService.getAvailableSlots(staffId, appointmentDate)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (slots) => {
          this.availableSlots = slots;
          this.bookingForm.patchValue({ startTime: '' });
        },
        error: () => {
          this.availableSlots = [];
        }
      });
  }

  onSubmit(): void {
    if (this.bookingForm.invalid) return;

    this.isSubmitting = true;
    this.errorMessage = '';

    this.appointmentService.createAppointment(this.bookingForm.value)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (appointment) => {
          this.isSubmitting = false;
          this.successMessage = 'Appointment booked successfully!';
          setTimeout(() => this.router.navigate(['/appointments']), 2000);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.errorMessage = err.error?.message || 'Failed to book appointment';
        }
      });
  }
}
