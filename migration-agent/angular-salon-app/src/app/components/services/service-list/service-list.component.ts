import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, catchError, finalize } from 'rxjs/operators';
import { EMPTY } from 'rxjs';
import { SalonServiceApi } from '../../../services/salon.service';
import { SalonService } from '../../../models/service.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-service-list',
  templateUrl: './service-list.component.html',
  styleUrls: ['./service-list.component.scss']
})
export class ServiceListComponent implements OnInit, OnDestroy {
  services: SalonService[] = [];
  filteredServices: SalonService[] = [];
  isLoading = false;
  errorMessage = '';
  searchQuery = '';
  selectedCategory = 'ALL';
  categories: string[] = [];

  private destroy$ = new Subject<void>();

  constructor(private salonService: SalonServiceApi, private router: Router) {}

  ngOnInit(): void {
    this.loadServices();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadServices(): void {
    this.isLoading = true;
    this.salonService.getServices()
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          this.errorMessage = err.error?.message || 'Failed to load services';
          return EMPTY;
        }),
        finalize(() => (this.isLoading = false))
      )
      .subscribe((services) => {
        this.services = services;
        this.filteredServices = services;
        this.categories = ['ALL', ...new Set(services.map((s) => s.category))];
      });
  }

  filterByCategory(category: string): void {
    this.selectedCategory = category;
    this.applyFilters();
  }

  onSearch(query: string): void {
    this.searchQuery = query;
    this.applyFilters();
  }

  private applyFilters(): void {
    this.filteredServices = this.services.filter((service) => {
      const matchesCategory =
        this.selectedCategory === 'ALL' || service.category === this.selectedCategory;
      const matchesSearch =
        !this.searchQuery ||
        service.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        service.description.toLowerCase().includes(this.searchQuery.toLowerCase());
      return matchesCategory && matchesSearch;
    });
  }

  bookService(serviceId: number): void {
    this.router.navigate(['/book'], { queryParams: { serviceId } });
  }

  formatPrice(price: number, currency: string): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(price);
  }

  trackById(index: number, service: SalonService): number {
    return service.id;
  }
}
