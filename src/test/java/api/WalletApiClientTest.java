package api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletApiClientTest {

    private ApiConfiguration config;
    private AuthenticationService authService;
    private HttpClient mockHttpClient;
    private WalletApiClient apiClient;

    @BeforeEach
    void setUp() {
        config = new ApiConfiguration("https://api.example.com", "test_key");
        authService = new AuthenticationService(config);
        mockHttpClient = mock(HttpClient.class);
        apiClient = new WalletApiClient(config, authService, mockHttpClient);
    }

    @Test
    void testGet_Success_ParsesJsonResponse() throws Exception {
        // Arrange
        String jsonResponse = "{\"id\": 1, \"name\": \"Test Category\"}";
        
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockResponse);

        // Act
        TestResponse result = apiClient.get("/test", TestResponse.class);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.id);
        assertEquals("Test Category", result.name);
    }

    @Test
    void testGet_ApiError_ThrowsApiException() throws Exception {
        // Arrange
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(401);
        when(mockResponse.body()).thenReturn("{\"error\": \"Unauthorized\"}");
        
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockResponse);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> apiClient.get("/test", TestResponse.class));
        assertEquals(401, exception.getStatusCode());
        assertEquals("{\"error\": \"Unauthorized\"}", exception.getResponseBody());
        assertTrue(exception.getMessage().contains("Status: 401"));
    }

    @Test
    void testGet_NetworkError_ThrowsApiException() throws Exception {
        // Arrange
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("Network failure"));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> apiClient.get("/test", TestResponse.class));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void testGet_InvalidEndpoint_ThrowsException() {
        // Assert
        assertThrows(IllegalArgumentException.class, () -> apiClient.get("invalid-endpoint", TestResponse.class));
        assertThrows(IllegalArgumentException.class, () -> apiClient.get(null, TestResponse.class));
    }

    // Dummy class for testing JSON deserialization
    static class TestResponse {
        public int id;
        public String name;
    }
}
