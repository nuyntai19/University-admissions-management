package vn.edu.sgu.phanmemtuyensinh.utils;

public final class DatabaseConfig {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB_NAME = "xettuyen2026";
    private static final String DEFAULT_USER = "root";

    private final String url;
    private final String username;
    private final String password;

    private DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static DatabaseConfig resolve() {
        String dbUrl = firstNonBlank(
                System.getProperty("db.url"),
                System.getenv("DB_URL"),
                System.getenv("HIBERNATE_URL"));

        if (dbUrl == null) {
            String host = firstNonBlank(
                    System.getProperty("db.host"),
                    System.getenv("DB_HOST"),
                    DEFAULT_HOST);
            String port = firstNonBlank(
                    System.getProperty("db.port"),
                    System.getenv("DB_PORT"),
                    DEFAULT_PORT);
            String dbName = firstNonBlank(
                    System.getProperty("db.name"),
                    System.getenv("DB_NAME"),
                    DEFAULT_DB_NAME);

            dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
        }

        String username = firstNonBlank(
                System.getProperty("db.user"),
                System.getenv("DB_USER"),
                System.getenv("HIBERNATE_USERNAME"),
                DEFAULT_USER);

        String password = firstNonBlank(
                System.getProperty("db.pass"),
                System.getenv("DB_PASS"),
                System.getenv("HIBERNATE_PASSWORD"),
                System.getenv("MYSQL_ROOT_PASSWORD"));

        if (password == null) {
            password = "";
        }

        return new DatabaseConfig(normalizeMysqlUrl(dbUrl), username, password);
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    private static String normalizeMysqlUrl(String rawUrl) {
        String normalized = rawUrl == null ? "" : rawUrl.trim();
        normalized = ensureQueryParam(normalized, "allowPublicKeyRetrieval", "true");
        normalized = ensureQueryParam(normalized, "useSSL", "false");
        normalized = ensureQueryParam(normalized, "serverTimezone", "UTC");
        normalized = ensureQueryParam(normalized, "useUnicode", "true");
        normalized = ensureQueryParam(normalized, "characterEncoding", "UTF-8");
        return normalized;
    }

    private static String ensureQueryParam(String url, String key, String value) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        String lowerUrl = url.toLowerCase();
        String lowerKey = key.toLowerCase() + "=";
        if (lowerUrl.contains(lowerKey)) {
            return url;
        }

        return url + (url.contains("?") ? "&" : "?") + key + "=" + value;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null) {
                String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        }
        return null;
    }
}