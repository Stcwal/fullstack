package backend.fullstack.config;

/**
 * Standard API response envelope used by controllers.
 *
 * @param <T> payload type for successful responses
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    /**
     * Creates an empty response instance.
     */
    public ApiResponse() {}

    /**
     * Creates a response instance with all fields set.
     *
     * @param success indicates if the operation succeeded
     * @param message human-readable message
     * @param data response payload
     */
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Creates a successful response with default message.
     *
     * @param data response payload
     * @param <T> payload type
     * @return success response
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    /**
     * Creates a successful response with a custom message.
     *
     * @param message custom success message
     * @param data response payload
     * @param <T> payload type
     * @return success response
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Creates an error response without payload.
     *
     * @param message error message
     * @param <T> payload type
     * @return error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * @return true when operation was successful
     */
    public boolean isSuccess() { return success; }

    /**
     * @param success operation outcome flag
     */
    public void setSuccess(boolean success) { this.success = success; }

    /**
     * @return response message
     */
    public String getMessage() { return message; }

    /**
     * @param message response message
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * @return response payload
     */
    public T getData() { return data; }

    /**
     * @param data response payload
     */
    public void setData(T data) { this.data = data; }
}
