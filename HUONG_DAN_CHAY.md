# Hướng Dẫn Chạy Hệ Thống Quản Lý Tuyển Sinh

## 1. Yêu Cầu Hệ Thống

- **Java**: JDK 21 LTS trở lên
- **Maven**: 5.2 trở lên
- **MySQL**: 8.0.32 trở lên (tạo database `xettuyen2026`)
- **Bộ nhớ**: Ít nhất 2GB RAM

## 2. Chuẩn Bị Database

### Bước 1: Tạo Database

Mở MySQL Command Line hoặc MySQL Workbench:

```sql
CREATE DATABASE xettuyen2026 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Bước 2: Import Schema

Chạy script SQL từ file:

```
database/xettuyen2026_empty.sql
```

hoặc

```
database/xettuyen2026_v2_with_users.sql  (nếu muốn sử dụng quản lý người dùng)
```

#### Script Tự Động (nếu tồn tại):

```bash
mysql -u root -p xettuyen2026 < database/xettuyen2026_v2_with_users.sql
```

### Bước 3: Đăng Nhập Mặc Định (nếu dùng v2)

- **Tài khoản admin**: `admin` / `admin123`
- **Tài khoản user**: `user01` / `user123`

## 3. Cấu Hình Ứng Dụng

### File: `src/main/resources/hibernate.cfg.xml`

Kiểm tra thông tin kết nối database:

```xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/xettuyen2026?useSSL=false&serverTimezone=UTC</property>
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">12345678</property>
```

**Là đổi `username` và `password` cho phù hợp với MySQL setup của bạn!**

## 4. Chạy Ứng Dụng

### Option A: Chạy với Maven từ Command Line

**Từ thư mục dự án (`PhanMemTuyenSinh`):**

```bash
# Build project
mvn clean compile

# Chạy ứng dụng
mvn exec:java -Dexec.mainClass="vn.edu.sgu.phanmemtuyensinh.PhanMemTuyenSinh"
```

### Option B: Chạy với IDE (NetBeans / VS Code + Maven Extension)

1. Mở project folder trong IDE
2. Chuỗt phải project → **Run as** → **Maven Build...**
3. Nhập command goal: `exec:java -Dexec.mainClass="vn.edu.sgu.phanmemtuyensinh.PhanMemTuyenSinh"`
4. Click **Run**

### Option C: Build JAR và Chạy Standalone

```bash
# Package ứng dụng thành JAR
mvn clean package -DskipTests

# Chạy JAR file
java -jar target/PhanMemTuyenSinh-1.0-SNAPSHOT.jar
```

## 5. Giao Diện Chính

Sau khi chạy, bạn sẽ thấy cửa sổ với các tab:

1. **Dashboard** - Thống kê tổng quan (9 stat cards)
2. **Quản Lý Người Dùng** - Thêm/Sửa/Xóa tài khoản, quản lý quyền
3. **Quản Lý Thí Sinh** - Nhập thông tin học sinh, tìm kiếm
4. **Quản Lý Ngành** - Quản lý chương trình tuyển sinh
5. **Quản Lý Ngành-Tổ Hợp** - Ánh xạ ngành với tổ hợp môn
6. **Quản Lý Điểm Thi** - Nhập điểm từ 3 phương thức (THPT/VSAT/ĐGNL)
7. **Quản Lý Điểm Cộng** - Nhập điểm ưu tiên & cộng điểm
8. **Quản Lý Nguyện Vọng** - Nhập nguyện vọng, xem kết quả
9. **Quản Lý Bảng Quy Đổi** - Quản lý của quy đổi điểm
10. **Quản Lý Tổ Hợp Môn** - Thêm/Sửa tổ hợp, **import Excel**

## 6. Nhập Dữ Liệu từ Excel

### Module: Quản Lý Tổ Hợp Môn

- Nút **Import Excel** (xanh) cho phép chọn file `.xlsx`
- Format Excel:
  ```
  Cột A: Mã Tổ Hợp (vd: B03)
  Cột B: Mô Tả Môn (vd: TO-3,VA-3,SI-1)
  ```

## 7. Phân Quyền Người Dùng

Khi tạo tài khoản, chọn **Phân Quyền**:

- `admin` - Full access tất cả module
- `user` - Access limited modules

## 8. Troubleshooting

### Lỗi "Cannot load driver class: com.mysql.cj.jdbc.Driver"

→ Kiểm tra MySQL Connector JAR trong `pom.xml` hoặc folder `lib/`

### Lỗi "Access denied for user 'root'@'localhost'"

→ Sửa password trong `hibernate.cfg.xml` phù hợp với MySQL setup

### Lỗi "Unknown database 'xettuyen2026'"

→ Tạo lại database theo hướng dẫn bước 2

### Lỗi Maven `release version 21 not supported`

→ Cập nhật Maven hoặc Java version lên >= 21

## 9. Mô Tả Dự Án

**Tên**: Hệ Thống Quản Lý Tuyển Sinh SGU  
**Công Nghệ**: Java Swing, Hibernate ORM, MySQL  
**Phiên Bản**: 1.0-SNAPSHOT  
**Tác Giả**: SGU Development Team

## 10. Liên Hệ & Hỗ Trợ

Nếu gặp vấn đề, kiểm tra:

1. Đảm bảo MySQL đang chạy
2. Database `xettuyen2026` tồn tại
3. Kiểm tra `hibernate.cfg.xml` credentials
4. Xem console log khi chạy mã

---

**Chúc bạn sử dụng vui vẻ!** 🚀
