export interface CustomerSummary {
  id: number;
  name: string;
  email: string;
  initials: string;
  tier: string;      // VIP | Regular | New | Inactive
  tierClass: string; // vip | regular | new | inactive
  orderCount: number;
  ltv: number;
  joinedDate: string;
}

export interface CustomerDetail {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  initials: string;
  tier: string;
  tierClass: string;
  orderCount: number;
  ltv: number;
  riskLevel: string;
  joinedDate: string;
  lastSeen: string;
  recentActivity: ActivityItem[];
}

export interface ActivityItem {
  icon: string;
  text: string;
  time: string;
  color: string;
}

export interface CustomerListParams {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
}
