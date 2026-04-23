package vn.edu.sgu.phanmemtuyensinh.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import vn.edu.sgu.phanmemtuyensinh.utils.DatabaseConfig;

public class ClearThiSinhTable {

    public static void main(String[] args) {
        DatabaseConfig config = DatabaseConfig.resolve();

        try (Connection conn = DriverManager.getConnection(
                config.getUrl(),
                config.getUsername(),
                config.getPassword());
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
