package api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.prefs.Preferences;

public class ApiConfiguration {
    private static final String DEFAULT_BASE_URL = "https://rest.budgetbakers.com/wallet/v1/api";
    private final String baseUrl;
    private String apiKey;

    public ApiConfiguration() {
        this.baseUrl = DEFAULT_BASE_URL;
        this.apiKey = loadApiKey();
    }

    public ApiConfiguration(String baseUrl, String apiKey) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be null or blank");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be null or blank");
        }
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    private String loadApiKey() {
        // 1. Try to load from Java Preferences
        Preferences prefs = Preferences.userNodeForPackage(ApiConfiguration.class);
        String savedKey = prefs.get("WALLET_API_KEY", null);
        if (savedKey != null && !savedKey.trim().isEmpty()) {
            return savedKey.trim();
        }
        
        // 2. Try to load from .env file
        try {
            Path envPath = Paths.get(".env");
            if (Files.exists(envPath)) {
                Properties props = new Properties();
                try (var inputStream = Files.newInputStream(envPath)) {
                    props.load(inputStream);
                }
                String key = props.getProperty("WALLET_API_KEY");
                if (key != null && !key.trim().isEmpty()) {
                    return key.trim();
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Failed to read .env file");
        }

        // 3. Fallback to environment variable
        String envKey = System.getenv("WALLET_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }

        return null;
    }

    public void saveApiKey(String newKey) {
        if (newKey == null || newKey.isBlank()) {
            throw new IllegalArgumentException("API key must not be null or blank");
        }
        this.apiKey = newKey.trim();
        Preferences prefs = Preferences.userNodeForPackage(ApiConfiguration.class);
        prefs.put("WALLET_API_KEY", this.apiKey);
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
