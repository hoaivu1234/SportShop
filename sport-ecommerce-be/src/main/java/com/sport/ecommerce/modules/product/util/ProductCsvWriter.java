package com.sport.ecommerce.modules.product.util;

import com.sport.ecommerce.modules.product.dto.response.ProductListResponse;

import java.time.format.DateTimeFormatter;

/**
 * Stateless utility that converts {@link ProductListResponse} objects into CSV rows.
 *
 * <p>RFC 4180 compliance:
 * <ul>
 *   <li>Fields containing comma, double-quote, or newline are wrapped in double-quotes.</li>
 *   <li>Embedded double-quotes are escaped by doubling them.</li>
 *   <li>A UTF-8 BOM ({@code \uFEFF}) is written once before the header so that
 *       Microsoft Excel opens the file without an encoding wizard.</li>
 * </ul>
 */
public final class ProductCsvWriter {

    public static final String BOM = "\uFEFF";

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ProductCsvWriter() {}

    /** CSV header line (no trailing newline — caller must append one). */
    public static String header() {
        return "ID,Name,Category,Brand,Price,Discount Price,Status,Total Stock,Created At";
    }

    /** Converts one product to a single CSV line (including trailing newline). */
    public static String toRow(ProductListResponse p) {
        return String.join(",",
                safe(p.getId()),
                escape(p.getName()),
                escape(p.getCategoryName()),
                escape(p.getBrand()),
                safe(p.getPrice()),
                safe(p.getDiscountPrice()),
                escape(p.getStatus()),
                safe(p.getTotalStock()),
                p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FMT) : ""
        ) + "\n";
    }

    /**
     * Wraps a value in double-quotes when it contains a comma, double-quote, or newline.
     * Embedded double-quotes are doubled per RFC 4180.
     */
    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String safe(Object value) {
        return value == null ? "" : value.toString();
    }
}
