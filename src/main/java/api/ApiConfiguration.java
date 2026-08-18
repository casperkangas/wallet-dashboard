package api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ApiConfiguration {
    private static final String DEFAULT_BASE_URL = "https://rest.budgetbakers.com/wallet";
    private final String baseUrl;
    private final String apiKey;

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
        // Try to load from .env file first
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

        // Fallback to environment variable
        String envKey = System.getenv("WALLET_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }

        throw new IllegalStateException("Wallet API key not found in .env file or WALLET_API_KEY environment variable");
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
