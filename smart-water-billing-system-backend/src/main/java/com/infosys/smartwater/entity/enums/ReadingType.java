package com.infosys.smartwater.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Indicates how a {@code WaterUsage} meter reading was recorded.
 *
 * <ul>
 *   <li>{@link #MANUAL}     — Entered directly by an ADMIN via the REST API.</li>
 *   <li>{@link #CSV_IMPORT} — Bulk-loaded from a CSV file upload (Task 13).</li>
 * </ul>
 *
 * <p>Useful for audit trails, identifying data quality issues, and filtering
 * readings by source when generating billing reports.</p>
 *
 * <p>Stored as a {@code VARCHAR(20)} in the database via {@code @Enumerated(EnumType.STRING)}.</p>
 */
public enum ReadingType {

    /**
     * Reading was entered manually by an administrator through the API.
     */
    MANUAL,

    /**
     * Reading was imported from a CSV file batch upload.
     */
    CSV_IMPORT;

    @JsonCreator
    public static ReadingType fromString(String value) {
        if (value == null || value.isBlank()) {
            return MANUAL;
        }
        String upper = value.trim().toUpperCase();
        for (ReadingType type : values()) {
            if (type.name().equals(upper)) {
                return type;
            }
        }
        return MANUAL;
    }
}
