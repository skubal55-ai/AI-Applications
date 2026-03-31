export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

export interface Appointment {
  id: number;
  customerId: number;
  customerName: string;
  staffId: number;
  staffName: string;
  serviceId: number;
  serviceName: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  status: AppointmentStatus;
  notes: string;
  totalPrice: number;
  currency: string;
  createdAt: string;
}

export interface CreateAppointmentRequest {
  staffId: number;
  serviceId: number;
  appointmentDate: string;
  startTime: string;
  notes?: string;
}
