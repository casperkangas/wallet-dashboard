package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WalletApiClient {
    private final ApiConfiguration configuration;
    private final AuthenticationService authService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WalletApiClient(ApiConfiguration configuration, AuthenticationService authService) {
        this(configuration, authService, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    // For testing purposes
    public WalletApiClient(ApiConfiguration configuration, AuthenticationService authService, HttpClient httpClient) {
        if (configuration == null || authService == null || httpClient == null) {
            throw new IllegalArgumentException("Dependencies cannot be null");
        }
        this.configuration = configuration;
        this.authService = authService;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Makes a GET request to the specified endpoint and parses the JSON response.
     *
     * @param endpoint     The API endpoint (e.g., "/records"). Must start with a slash.
     * @param responseType The class to deserialize the JSON response into.
     * @param <T>          The type of the response.
     * @return The deserialized response object.
     * @throws ApiException if an API or network error occurs.
     */
    public <T> T get(String endpoint, Class<T> responseType) {
        if (endpoint == null || !endpoint.startsWith("/")) {
            throw new IllegalArgumentException("Endpoint must start with '/'");
        }

        String url = configuration.getBaseUrl() + endpoint;

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json");

        HttpRequest request = authService.applyAuthentication(requestBuilder).build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(response.body());
                String resourceKey = endpoint.split("\\?")[0].substring(1);
                
                if (rootNode.has(resourceKey)) {
                    return objectMapper.treeToValue(rootNode.get(resourceKey), responseType);
                } else {
                    return objectMapper.treeToValue(rootNode, responseType);
                }
            } else {
                throw new ApiException("API Request failed", response.statusCode(), response.body());
            }
        } catch (IOException e) {
            throw new ApiException("Network or parsing error during GET request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Thread interrupted during GET request", e);
        }
    }
}
