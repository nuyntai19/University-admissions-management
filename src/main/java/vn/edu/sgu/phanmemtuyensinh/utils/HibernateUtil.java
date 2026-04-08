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
            final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure() // Tự động đọc file hibernate.cfg.xml
                    .build();
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            initializationException = new IllegalStateException(
                    "Khong the khoi tao Hibernate SessionFactory. Kiem tra file hibernate.cfg.xml, ket noi MySQL, va entity mapping.",
                    e
            );
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
}