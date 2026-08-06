export type Role = 'SENIOR' | 'VOLUNTEER' | 'ADMIN';

export interface UserRecord {
  id: number; email: string; firstName: string; lastName: string; phone?: string; role: Role; active: boolean;
}

export interface MealRequest {
  id: number; seniorId: number; seniorName: string; seniorPhone?: string; requestedDeliveryDate: string;
  mealType: string; quantity: number; dietaryNotes?: string; deliveryAddress: string; status: string;
  assignedVolunteerId?: number; assignedVolunteerName?: string; adminNotes?: string; completionNotes?: string;
}

export interface CompanionRequest {
  id: number; seniorId: number; seniorName: string; seniorPhone?: string; seniorAddress?: string;
  requestedDate: string; requestedTime: string; reason: string; serviceNotes?: string; status: string;
  scheduledAt?: string; assignedVolunteerId?: number; assignedVolunteerName?: string; adminNotes?: string; completionNotes?: string;
}

export interface NotificationRecord { id: number; title: string; message: string; read: boolean; createdAt: string; }
export interface DashboardSummary { totalUsers: number; activeUsers: number; pendingMealRequests: number; pendingCompanionRequests: number; completedDeliveries: number; completedCompanions: number; }
