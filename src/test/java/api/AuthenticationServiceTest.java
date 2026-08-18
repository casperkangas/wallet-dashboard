package api;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpRequest;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    @Test
    void testApplyAuthentication_AddsBearerToken() {
        // Arrange
        ApiConfiguration config = new ApiConfiguration("https://example.com", "test_api_key");
        AuthenticationService authService = new AuthenticationService(config);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create("https://example.com/test"));

        // Act
        HttpRequest request = authService.applyAuthentication(builder).build();

        // Assert
        assertTrue(request.headers().map().containsKey("Authorization"));
        assertEquals("Bearer test_api_key", request.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    void testConstructor_NullConfiguration_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new AuthenticationService(null));
    }
    
    @Test
    void testApplyAuthentication_NullBuilder_ThrowsException() {
        ApiConfiguration config = new ApiConfiguration("https://example.com", "test_api_key");
        AuthenticationService authService = new AuthenticationService(config);
        assertThrows(IllegalArgumentException.class, () -> authService.applyAuthentication(null));
    }
}
