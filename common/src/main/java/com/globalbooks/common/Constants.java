package main.java.com.globalbooks.common;

/**
 * Common constants used across services
 */
public class Constants {

    public static final String API_VERSION = "v1";

    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";
    public static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    private Constants() {
        // Utility class
    }
}