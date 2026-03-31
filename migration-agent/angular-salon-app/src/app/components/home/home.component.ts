import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { SalonServiceApi } from '../../services/salon.service';
import { SalonService, Staff } from '../../models/service.model';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  currentUser: User | null = null;
  featuredServices: SalonService[] = [];
  topStaff: Staff[] = [];
  isLoading = false;

  testimonials = [
    {
      name: 'Sarah M.',
      rating: 5,
      comment: 'Amazing service! The staff is professional and the atmosphere is wonderful.'
    },
    {
      name: 'John D.',
      rating: 5,
      comment: 'Best salon in town. Highly recommended!'
    },
    {
      name: 'Emily R.',
      rating: 4,
      comment: 'Great experience, will definitely come back.'
    }
  ];

  constructor(
    private authService: AuthService,
    private salonService: SalonServiceApi
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadFeaturedData();
  }

  loadFeaturedData(): void {
    this.isLoading = true;
    this.salonService.getServices().subscribe({
      next: (services) => {
        this.featuredServices = services.slice(0, 3);
        this.isLoading = false;
      },
      error: () => (this.isLoading = false)
    });

    this.salonService.getStaff().subscribe({
      next: (staff) => {
        this.topStaff = staff.slice(0, 4);
      }
    });
  }

  getStarArray(rating: number): number[] {
    return Array(rating).fill(0);
  }
}
