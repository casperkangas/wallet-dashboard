package api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiConfigurationTest {

    @Test
    void testApiKeyIsLoaded() {
        // This test relies on the .env file being present in the project root
        // as configured by the user, or the WALLET_API_KEY environment variable.
        ApiConfiguration config = new ApiConfiguration();
        
        String apiKey = config.getApiKey();
        assertNotNull(apiKey, "API key should not be null");
        assertFalse(apiKey.trim().isEmpty(), "API key should not be empty");
    }

    @Test
    void testBaseUrl() {
        ApiConfiguration config = new ApiConfiguration();
        assertEquals("https://rest.budgetbakers.com/wallet", config.getBaseUrl());
    }
}
