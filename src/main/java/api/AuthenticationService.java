package api;

import java.net.http.HttpRequest;

public class AuthenticationService {
    private final ApiConfiguration configuration;

    public AuthenticationService(ApiConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("ApiConfiguration cannot be null");
        }
        this.configuration = configuration;
    }

    /**
     * Applies the required authentication headers to the given HttpRequest builder.
     * The Wallet API uses a Bearer Token.
     */
    public HttpRequest.Builder applyAuthentication(HttpRequest.Builder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("HttpRequest.Builder cannot be null");
        }
        return builder.header("Authorization", "Bearer " + configuration.getApiKey());
    }
}
