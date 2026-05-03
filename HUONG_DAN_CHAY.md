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

File này đã được đổi sang placeholder và sẽ được `HibernateUtil` override từ biến môi trường/System property:

```xml
<property name="hibernate.connection.url">${db.url}</property>
<property name="hibernate.connection.username">${db.user}</property>
<property name="hibernate.connection.password">${db.pass}</property>
```

Có thể cấu hình bằng một trong 2 cách:

- Biến môi trường: `DB_URL` hoặc bộ `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`
- JVM args: `-Ddb.url=... -Ddb.user=... -Ddb.pass=...`

Nếu không truyền `DB_URL`, hệ thống sẽ tự động ghép URL MySQL và bổ sung các tham số kết nối an toàn như `allowPublicKeyRetrieval=true`.

## 4. Chạy Ứng Dụng

### Option A: Chạy với Maven từ Command Line

**Từ thư mục dự án (`PhanMemTuyenSinh`):**

```bash
# Build project
mvn clean compile

# Chạy ứng dụng (đã có cấu hình DB mặc định trong pom.xml)
mvn exec:java
```

Mặc định `mvn exec:java` sẽ dùng:

- `db.url=jdbc:mysql://localhost:3306/xettuyen2026`
- `db.user=root`
- `db.pass=12345678`

Nếu muốn override tạm thời theo máy khác:

```bash
mvn exec:java "-Ddb.url=jdbc:mysql://localhost:3307/xettuyen2026" "-Ddb.user=root" "-Ddb.pass=mat_khau_khac"
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

### Option D: Docker (build image)

Project đã có sẵn `Dockerfile` để **build JAR + gom đủ thư viện runtime**.

```bash
# Build image
docker build -t phanmemtuyensinh:latest .
```

#### Cấu hình kết nối MySQL khi chạy trong container

Ứng dụng hỗ trợ override cấu hình DB bằng biến môi trường (không cần sửa `hibernate.cfg.xml`):

- `DB_URL` (ưu tiên cao nhất) — ví dụ: `jdbc:mysql://host.docker.internal:3306/xettuyen2026?...`
- hoặc tách rời: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`

Ví dụ chạy (chỉ minh hoạ cấu hình DB):

```bash
docker run --rm \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=3306 \
  -e DB_NAME=xettuyen2026 \
  -e DB_USER=root \
  -e DB_PASS=12345678 \
  phanmemtuyensinh:latest
```

Lưu ý: đây là ứng dụng **Java Swing (GUI)**, nên để hiển thị giao diện trong Docker bạn cần cấu hình thêm X server/GUI forwarding (phụ thuộc hệ điều hành). Trên Windows thường sẽ tiện hơn nếu chạy GUI trực tiếp trên máy host và chỉ dùng Docker cho MySQL/build.

### Option E: Docker Compose (MySQL cho dự án)

Repo có sẵn `docker-compose.yml` để khởi động MySQL và tự tạo database `xettuyen2026`.

```bash
# Start MySQL
docker compose up -d db

# Stop
docker compose down
```

Mặc định MySQL sẽ được map ra host port `3307` (để tránh đụng MySQL local). Nếu muốn dùng port `3306`:

```powershell
$env:MYSQL_HOST_PORT=3306
docker compose up -d db
```

CMD (Command Prompt):

```bat
set MYSQL_HOST_PORT=3306
docker compose up -d db
```

Bạn cũng có thể đổi mật khẩu root của MySQL:

```powershell
$env:MYSQL_ROOT_PASSWORD="your_password"
docker compose up -d db
```

Nếu bạn chạy app trên máy host và DB chạy trong Docker ở port `3307`, hãy cấu hình lại DB bằng biến môi trường (ví dụ `DB_PORT=3307` hoặc `DB_URL=...`).

Ghi chú: file SQL init trong `docker-compose.yml` chỉ chạy **lần đầu** khi volume DB còn trống. Nếu muốn import lại từ đầu:

```bash
docker compose down -v
docker compose up -d db
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

→ Kiểm tra thông tin `db.user` và `db.pass` trong `pom.xml` (hoặc truyền `-Ddb.user/-Ddb.pass` khi chạy)

### Lỗi "Unknown database 'xettuyen2026'"

→ Tạo lại database theo hướng dẫn bước 2

### Lỗi Maven `release version 21 not supported`

→ Cập nhật Maven hoặc Java version lên >= 21

### Maven đang dùng Java 8 (mở ra lỗi cú pháp như `')' expected`, `illegal start of expression`)

→ Kiểm tra `mvn -v` xem đang chạy Java version nào.

- Nếu thấy `Java version: 1.8...` thì Maven đang dùng JDK 8 (không phù hợp với dự án).
- Cách nhanh (PowerShell) để build/chạy bằng JDK mới trong phiên terminal hiện tại:

```bash
$env:JAVA_HOME="C:\\Path\\To\\JDK"  # ví dụ JDK 21 hoặc JDK 17
$env:Path="$env:JAVA_HOME\\bin;" + $env:Path
mvn -v
mvn -DskipTests package
```

→ Để fix lâu dài: đặt biến môi trường `JAVA_HOME` của Windows trỏ tới JDK mới (>= 17, khuyến nghị 21) và đảm bảo `%JAVA_HOME%\\bin` đứng trước Java cũ trong `Path`.

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
