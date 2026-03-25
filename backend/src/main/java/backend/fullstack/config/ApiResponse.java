package backend.fullstack.config;

/**
 * A generic wrapper for all API responses to ensure a consistent JSON structure.
 *
 * @param <T>     The type of the data payload
 * @param success Whether the API call was successful
 * @param message A descriptive message about the result
 * @param data    The actual payload (can be null for errors or simple success messages)
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    // Static factory method for successful responses with data and a custom message
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    // Static factory method for successful responses with just data
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    // Static factory method for error responses
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}