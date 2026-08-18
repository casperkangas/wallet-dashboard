package database;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseConfiguration {
    private static final String APP_NAME = "WalletDashboard";
    private static final String DB_FILENAME = "wallet_dashboard.db";

    public String getDatabasePath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        Path dirPath;

        if (os.contains("mac")) {
            dirPath = Paths.get(userHome, "Library", "Application Support", APP_NAME);
        } else if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData == null || appData.isEmpty()) {
                dirPath = Paths.get(userHome, "AppData", "Roaming", APP_NAME);
            } else {
                dirPath = Paths.get(appData, APP_NAME);
            }
        } else {
            // Linux/Unix fallback
            dirPath = Paths.get(userHome, ".local", "share", APP_NAME);
        }

        File dir = dirPath.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dirPath.resolve(DB_FILENAME).toAbsolutePath().toString();
    }

    public String getJdbcUrl() {
        return "jdbc:sqlite:" + getDatabasePath();
    }
}
