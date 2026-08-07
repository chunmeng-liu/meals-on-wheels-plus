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

export type RoboCompanionStatus = 'AVAILABLE' | 'ASSIGNED' | 'IN_SERVICE' | 'MAINTENANCE' | 'INACTIVE';
export interface RoboCompanion {
  id: number; name: string; assetTag: string; model: string; description?: string; status: RoboCompanionStatus;
  active: boolean; notes?: string; createdAt: string; updatedAt: string;
}
export interface RoboCompanionVisit {
  id: number; seniorId: number; seniorName: string; seniorPhone?: string; seniorAddress?: string;
  requestedDate: string; requestedTime: string; reason: string; assistanceNeeds?: string; serviceNotes?: string;
  status: string; scheduledAt?: string; assignedRoboCompanionId?: number; assignedRoboCompanionName?: string;
  assignedRoboCompanionModel?: string; assignedRoboCompanionAssetTag?: string; adminNotes?: string;
  completionNotes?: string; createdAt: string; updatedAt: string;
}

export interface NotificationRecord { id: number; title: string; message: string; read: boolean; createdAt: string; }
export interface DashboardSummary { totalUsers: number; activeUsers: number; pendingMealRequests: number; pendingCompanionRequests: number; completedDeliveries: number; completedCompanions: number; totalRoboCompanions: number; availableRoboCompanions: number; roboCompanionsInService: number; pendingRoboCompanionRequests: number; scheduledRoboCompanionVisits: number; }
