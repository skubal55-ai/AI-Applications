import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Appointment, CreateAppointmentRequest } from '../models/appointment.model';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private apiUrl = `${environment.apiUrl}/appointments`;

  constructor(private http: HttpClient) {}

  getMyAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.apiUrl}/my`);
  }

  getAllAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(this.apiUrl);
  }

  getAppointmentById(id: number): Observable<Appointment> {
    return this.http.get<Appointment>(`${this.apiUrl}/${id}`);
  }

  createAppointment(request: CreateAppointmentRequest): Observable<Appointment> {
    return this.http.post<Appointment>(this.apiUrl, request);
  }

  cancelAppointment(id: number): Observable<Appointment> {
    return this.http.put<Appointment>(`${this.apiUrl}/${id}/cancel`, {});
  }

  confirmAppointment(id: number): Observable<Appointment> {
    return this.http.put<Appointment>(`${this.apiUrl}/${id}/confirm`, {});
  }

  getAvailableSlots(staffId: number, date: string): Observable<string[]> {
    const params = new HttpParams()
      .set('staffId', staffId.toString())
      .set('date', date);
    return this.http.get<string[]>(`${this.apiUrl}/available-slots`, { params });
  }
}
