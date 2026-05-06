package vn.edu.sgu.tuyensinhweb.dto;

/**
 * DTO cho form đăng nhập thí sinh.
 */
public class LoginForm {
    private String username;  // CCCD
    private String password;  // Ngày sinh ddMMyyyy

    public LoginForm() {}

    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }

    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
}
