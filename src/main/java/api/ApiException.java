package api;

public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public ApiException(String message) {
        super(message);
        this.statusCode = -1;
        this.responseBody = null;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
    }

    public ApiException(String message, int statusCode, String responseBody) {
        super(message + " (Status: " + statusCode + ")");
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
