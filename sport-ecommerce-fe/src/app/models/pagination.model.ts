export type SortDirection = 'asc' | 'desc';

export interface PaginationParams {
  page: number;
  size: number;
  sort?: string;
  direction?: SortDirection;
}

export interface PaginationMeta {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  isFirst: boolean;
  isLast: boolean;
  numberOfElements: number;
}

export interface SortOption {
  field: string;
  direction: SortDirection;
  label?: string;
}

export const DEFAULT_PAGINATION: Readonly<PaginationParams> = {
  page: 0,
  size: 12,
  sort: 'createdAt',
  direction: 'desc',
};
