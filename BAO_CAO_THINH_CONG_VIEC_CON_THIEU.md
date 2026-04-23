# Báo cáo phần việc của Thịnh còn thiếu

Tài liệu này được tổng hợp sau khi đối chiếu file phân công và quét nhanh các module trong project hiện tại.

## 1. Phần đã có trong project

Các đầu mục thuộc phạm vi của Thịnh hiện đã thấy trong code:

- Quản lý tài khoản: `NguoiDungGUI.java`, `NguoiDungBUS.java`, `NguoiDungDAO.java`, `NguoiDung.java`
- Đăng nhập: `DangNhapDialog.java`
- Điểm thi xét tuyển: `DiemThiXetTuyenGUI.java`, `DiemThiXetTuyenBUS.java`, `DiemThiXetTuyenDAO.java`, `DiemThiXetTuyen.java`
- Cấu hình Hibernate: `HibernateUtil.java`, `hibernate.cfg.xml`
- Tiện ích dọn dữ liệu test: `ClearThiSinhTable.java`

## 2. Những phần còn thiếu hoặc chưa hoàn chỉnh

### 2.1. Chưa có bộ test end-to-end thật sự

- Không thấy thư mục `src/test` hoặc file test Java nào trong project.
- Không có test tích hợp tự động để kiểm tra luồng hoàn chỉnh như: đăng nhập -> load dữ liệu -> CRUD -> import -> kiểm tra lại dữ liệu.
- `ClearThiSinhTable.java` chỉ là tiện ích xóa dữ liệu bảng thí sinh, chưa phải là một bộ test end-to-end.

### 2.2. Cấu hình Hibernate/MySQL còn thiếu tham số để login ổn định

- Trong `hibernate.cfg.xml`, URL kết nối MySQL vẫn chưa có `allowPublicKeyRetrieval=true`.
- Điều này khớp với lỗi đã gặp khi chạy app: `Public Key Retrieval is not allowed`.
- `HibernateUtil.java` đã có cơ chế override URL với `allowPublicKeyRetrieval=true`, nhưng file cấu hình gốc vẫn chưa được đồng bộ, nên chạy theo cấu hình mặc định vẫn lỗi.

### 2.3. Cấu hình kết nối vẫn còn hardcode

- `hibernate.cfg.xml` đang để sẵn `localhost`, `root`, `12345678`.
- `ClearThiSinhTable.java` cũng đang hardcode cùng kiểu thông tin kết nối.
- Nếu chuyển máy, đổi mật khẩu MySQL, hoặc triển khai môi trường khác thì các phần này sẽ dễ hỏng nếu chưa chuyển sang cấu hình ngoài.

### 2.4. Chưa có quy trình kiểm thử chuẩn sau import dữ liệu

- Có các màn import và xử lý dữ liệu, nhưng chưa thấy một quy trình kiểm thử được chuẩn hóa để xác nhận import xong thì hệ thống vẫn chạy đúng.
- Hiện tại phần này mới dừng ở mức code xử lý, chưa thành tài liệu test hoặc script chạy lại được.

## 3. Kết luận ngắn

Nếu bám đúng file phân công, phần của Thịnh đã có khung chính, nhưng còn thiếu rõ nhất 3 việc:

1. Bộ test end-to-end tự động.
2. Đồng bộ lại cấu hình MySQL trong `hibernate.cfg.xml` để tránh lỗi kết nối.
3. Chuẩn hóa quy trình reset và kiểm tra lại hệ thống sau khi import dữ liệu.

## 4. Ưu tiên nên làm tiếp

1. Sửa `hibernate.cfg.xml` để kết nối MySQL ổn định hơn.
2. Tạo test tích hợp hoặc script kiểm tra luồng login/CRUD/import.
3. Gom phần reset dữ liệu và kiểm tra sau import thành một quy trình rõ ràng.

## 5. Thứ Tự Bắt Đầu

Nếu bắt đầu lại từ đầu, nên đi theo thứ tự này:

1. Chốt cấu hình kết nối MySQL/Hibernate để app chạy được ổn định.
2. Kiểm tra luồng đăng nhập và phân quyền admin/user, vì đây là cửa vào của toàn hệ thống.
3. Rà module điểm thi xét tuyển, đặc biệt phần import và validate dữ liệu.
4. Hoàn thiện tiện ích reset dữ liệu test để phục vụ kiểm tra lặp lại.
5. Viết test end-to-end hoặc ít nhất là một script smoke test cho toàn luồng.