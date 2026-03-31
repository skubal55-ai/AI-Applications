import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SalonService, Staff, SubscriptionPlan } from '../models/service.model';

@Injectable({ providedIn: 'root' })
export class SalonServiceApi {
  constructor(private http: HttpClient) {}

  getServices(): Observable<SalonService[]> {
    return this.http.get<SalonService[]>(`${environment.apiUrl}/services`);
  }

  getServiceById(id: number): Observable<SalonService> {
    return this.http.get<SalonService>(`${environment.apiUrl}/services/${id}`);
  }

  getStaff(): Observable<Staff[]> {
    return this.http.get<Staff[]>(`${environment.apiUrl}/staff`);
  }

  getStaffById(id: number): Observable<Staff> {
    return this.http.get<Staff>(`${environment.apiUrl}/staff/${id}`);
  }

  getSubscriptionPlans(): Observable<SubscriptionPlan[]> {
    return this.http.get<SubscriptionPlan[]>(`${environment.apiUrl}/subscriptions/plans`);
  }

  subscribe(planId: number): Observable<{ success: boolean; message: string }> {
    return this.http.post<{ success: boolean; message: string }>(
      `${environment.apiUrl}/subscriptions/subscribe`,
      { planId }
    );
  }
}
