import { Params } from '@angular/router';
import { ProductListParams } from '../../../models/product.model';

// ── Domain types ──────────────────────────────────────────────────────────────

export type SortKey = 'newest' | 'price_asc' | 'price_desc';

/**
 * Single source of truth for all filter & pagination state.
 * This model lives in the URL; components read from and write to it
 * only through the parent's navigate() helper.
 *
 * categorySlug  — set by navbar links (e.g. "football"), maps to ?category=
 * categoryId    — set by the sidebar filter (numeric ID), maps to ?categoryId=
 * The two are mutually exclusive; categorySlug takes priority in API calls.
 */
export interface ProductFilterState {
  keyword:      string;
  categoryId:   number | null;
  categorySlug: string | null;
  brand:        string | null;
  minPrice:     number | null;
  maxPrice:     number | null;
  onSale:       boolean;
  sort:         SortKey;
  page:         number;   // 0-based (UI/URL uses 1-based via toQueryParams)
}

// ── Constants ─────────────────────────────────────────────────────────────────

export const MAX_PRICE_CAP = 500;

export const DEFAULT_FILTER: Readonly<ProductFilterState> = {
  keyword:      '',
  categoryId:   null,
  categorySlug: null,
  brand:        null,
  minPrice:     null,
  maxPrice:     null,
  onSale:       false,
  sort:         'newest',
  page:         0,
};

/** Maps a SortKey to the field name and direction the backend expects. */
const SORT_MAP: Record<SortKey, { sortBy: string; sortDir: 'asc' | 'desc' }> = {
  newest:     { sortBy: 'createdAt', sortDir: 'desc' },
  price_asc:  { sortBy: 'price',     sortDir: 'asc'  },
  price_desc: { sortBy: 'price',     sortDir: 'desc' },
};

const VALID_SORT_KEYS = new Set<SortKey>(['newest', 'price_asc', 'price_desc']);

// ── Serialization helpers ─────────────────────────────────────────────────────

/**
 * Deserialize Angular router Params into a ProductFilterState.
 * Missing or invalid values fall back to DEFAULT_FILTER values.
 */
export function parseQueryParams(params: Params): ProductFilterState {
  const sort = VALID_SORT_KEYS.has(params['sort'] as SortKey)
    ? (params['sort'] as SortKey)
    : DEFAULT_FILTER.sort;

  return {
    keyword:      typeof params['keyword']  === 'string' ? params['keyword'] : '',
    categoryId:   params['categoryId'] != null ? +params['categoryId'] : null,
    categorySlug: typeof params['category'] === 'string' ? params['category'] : null,
    brand:        typeof params['brand']    === 'string' ? params['brand']   : null,
    minPrice:     params['minPrice']  != null ? +params['minPrice']  : null,
    maxPrice:     params['maxPrice']  != null ? +params['maxPrice']  : null,
    onSale:       params['sale'] === 'true',
    sort,
    // URL page is 1-based for readability; internally 0-based
    page:         params['page'] != null ? Math.max(0, +params['page'] - 1) : 0,
  };
}

/**
 * Serialize a ProductFilterState to Angular router Params.
 * Default values are omitted to keep URLs clean.
 * categorySlug and categoryId are mutually exclusive in the URL.
 */
export function toQueryParams(state: ProductFilterState): Params {
  const p: Params = {};

  if (state.keyword)                p['keyword']    = state.keyword;
  // categorySlug (navbar) takes priority; sidebar categoryId is only written when no slug
  if (state.categorySlug)           p['category']   = state.categorySlug;
  else if (state.categoryId != null) p['categoryId'] = state.categoryId;
  if (state.brand)                  p['brand']      = state.brand;
  if (state.minPrice != null)       p['minPrice']   = state.minPrice;
  if (state.maxPrice != null)       p['maxPrice']   = state.maxPrice;
  if (state.onSale)                 p['sale']       = 'true';
  if (state.sort !== 'newest')      p['sort']       = state.sort;
  if (state.page > 0)               p['page']       = state.page + 1;

  return p;
}

/**
 * Transform a ProductFilterState into the ProductListParams shape
 * the ProductService expects.
 */
export function toApiParams(state: ProductFilterState, pageSize: number): ProductListParams {
  const { sortBy, sortDir } = SORT_MAP[state.sort];

  return {
    page:         state.page,
    size:         pageSize,
    sort:         sortBy,
    direction:    sortDir,
    keyword:      state.keyword      || undefined,
    categorySlug: state.categorySlug ?? undefined,
    categoryId:   state.categorySlug ? undefined : (state.categoryId ?? undefined),
    brand:        state.brand        ?? undefined,
    minPrice:     state.minPrice     ?? undefined,
    maxPrice:     state.maxPrice     ?? undefined,
    onSale:       state.onSale       || undefined,
  };
}
