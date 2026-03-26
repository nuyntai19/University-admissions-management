package vn.edu.sgu.phanmemtuyensinh.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ClearThiSinhTable {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/xettuyen2026?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            long before = getCount(stmt);
            stmt.executeUpdate("TRUNCATE TABLE xt_thisinhxettuyen25");
            long after = getCount(stmt);

            System.out.println("before_count=" + before);
            System.out.println("after_count=" + after);
        } catch (Exception ex) {
            System.err.println("clear_failed=" + ex.getMessage());
            System.exit(1);
        }
    }

    private static long getCount(Statement stmt) throws Exception {
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM xt_thisinhxettuyen25")) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
