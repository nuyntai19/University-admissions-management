package vn.edu.sgu.phanmemtuyensinh.utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static RuntimeException initializationException;

    static {
        try {
            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
                    .configure(); // Tự động đọc file hibernate.cfg.xml
            applyDbOverrides(registryBuilder);

            final StandardServiceRegistry registry = registryBuilder.build();
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            initializationException = new IllegalStateException(
                    "Khong the khoi tao Hibernate SessionFactory. Kiem tra file hibernate.cfg.xml, ket noi MySQL, va entity mapping.",
                    e);
            System.err.println("Loi khoi tao SessionFactory:");
            initializationException.printStackTrace(System.err);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw initializationException != null
                    ? initializationException
                    : new IllegalStateException("SessionFactory chua duoc khoi tao.");
        }
        return sessionFactory;
    }

    private static void applyDbOverrides(StandardServiceRegistryBuilder builder) {
        String dbUrl = firstNonBlank(
                System.getProperty("db.url"),
                System.getenv("DB_URL"),
                System.getenv("HIBERNATE_URL"));

        String host = firstNonBlank(System.getProperty("db.host"), System.getenv("DB_HOST"));
        String port = firstNonBlank(System.getProperty("db.port"), System.getenv("DB_PORT"));
        String dbName = firstNonBlank(System.getProperty("db.name"), System.getenv("DB_NAME"));

        if (dbUrl == null && host != null && dbName != null) {
            String effectivePort = port == null ? "3306" : port;
            dbUrl = "jdbc:mysql://" + host + ":" + effectivePort + "/" + dbName
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
        }

        String username = firstNonBlank(
                System.getProperty("db.user"),
                System.getenv("DB_USER"),
                System.getenv("HIBERNATE_USERNAME"));
        String password = firstNonBlank(
                System.getProperty("db.pass"),
                System.getenv("DB_PASS"),
                System.getenv("HIBERNATE_PASSWORD"));

        if (dbUrl != null) {
            builder.applySetting("hibernate.connection.url", dbUrl);
        }
        if (username != null) {
            builder.applySetting("hibernate.connection.username", username);
        }
        if (password != null) {
            builder.applySetting("hibernate.connection.password", password);
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null) {
                String s = v.trim();
                if (!s.isEmpty()) {
                    return s;
                }
            }
        }
        return null;
    }
}