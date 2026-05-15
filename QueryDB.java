import java.sql.*;
public class QueryDB {
    public static void main(String[] args) throws Exception {
        Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/xettuyen2026?user=root&password=12345678");
        ResultSet rs = c.createStatement().executeQuery("SELECT iddiemthi, d_phuongthuc, `TO`, LI, HO, NL1, vsat_to FROM xt_diemthixettuyen WHERE cccd='079205015972'");
        while(rs.next()) {
            System.out.println("PT: " + rs.getString("d_phuongthuc") + ", TO: " + rs.getString("TO") + ", NL1: " + rs.getString("NL1") + ", VSAT_TO: " + rs.getString("vsat_to"));
        }
    }
}
