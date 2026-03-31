export interface SalonService {
  id: number;
  name: string;
  description: string;
  duration: number;
  price: number;
  currency: string;
  category: string;
  imageUrl: string;
  isActive: boolean;
}

export interface Staff {
  id: number;
  firstName: string;
  lastName: string;
  specialization: string;
  bio: string;
  imageUrl: string;
  rating: number;
  isAvailable: boolean;
}

export interface SubscriptionPlan {
  id: number;
  name: string;
  description: string;
  price: number;
  currency: string;
  billingPeriod: 'MONTHLY' | 'YEARLY';
  features: string[];
  isPopular: boolean;
}
