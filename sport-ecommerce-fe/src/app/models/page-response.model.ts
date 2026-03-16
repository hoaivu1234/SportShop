export interface SpringSort {
  sorted: boolean;
  unsorted: boolean;
  empty: boolean;
}

export interface SpringPageable {
  pageNumber: number;
  pageSize: number;
  sort: SpringSort;
  offset: number;
  paged: boolean;
  unpaged: boolean;
}

/** Matches Spring Boot's Page<T> response format */
export interface PageResponse<T> {
  content: T[];
  pageable: SpringPageable;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  sort: SpringSort;
  numberOfElements: number;
  empty: boolean;
}

/** Wrapped in ApiResponse envelope */
export interface PageApiResponse<T> {
  success: boolean;
  message: string;
  data: PageResponse<T>;
  timestamp?: string;
}
