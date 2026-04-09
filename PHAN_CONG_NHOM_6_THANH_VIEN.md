# Phân Công Nhóm 6 Thành Viên - Hệ Thống Quản Lý Tuyển Sinh SGU
## 4. Phân Công Chi Tiết Cho 6 Thành Viên

**Hạn hoàn thành: hết ngày 17/4**

### 4.1. Tài - Thí Sinh + Chuẩn Bị Dữ Liệu Đầu Vào
Tài vẫn giữ vai trò đầu mối phần thí sinh, nhưng được mở rộng thêm phần dữ liệu đầu vào gần kề để cân bằng khối lượng.

**Phạm vi phụ trách:**
- `ThiSinhGUI.java`
- `ThiSinhBUS.java`
- `ThiSinhDAO.java`
- `ThiSinh.java`
- `ToHopMonGUI.java`
- `ToHopMonBUS.java`
- `ToHopMonDAO.java`
- `ToHopMon.java`
- Import dữ liệu thí sinh
- Tìm kiếm theo CCCD / họ tên
- Validate dữ liệu thí sinh
- Import / chuẩn hóa tổ hợp môn đầu vào

**Việc cần làm kỹ:**
- Chuẩn hóa mapping từ file `Ds thi sinh.txt`
- Làm rõ các cột như `N1`, `NL1`, `NK1...NK10`, `ĐTƯT`, `KVƯT`
- Xử lý import lỗi / thiếu cột / dữ liệu NaN
- Đồng bộ dữ liệu tổ hợp môn để phục vụ xét tuyển

### 4.2. Thịnh - Tài Khoản + Điểm Thi + Cấu Hình + Test End-to-End
Thịnh đã từng đảm nhận mảng này ở app trước, nên tiếp tục làm đầu mối, đồng thời phụ trách phần cấu hình và kiểm thử tích hợp để đảm bảo hệ thống chạy ổn định.

**Phạm vi phụ trách:**
- `NguoiDungGUI.java`
- `NguoiDungBUS.java`
- `NguoiDungDAO.java`
- `NguoiDung.java`
- `DiemThiXetTuyenGUI.java`
- `DiemThiXetTuyenBUS.java`
- `DiemThiXetTuyenDAO.java`
- `DiemThiXetTuyen.java`
- `DangNhapDialog.java`
- `HibernateUtil.java`
- Cấu hình liên quan trong `hibernate.cfg.xml`
- Hỗ trợ test end-to-end toàn hệ thống
- Dọn dữ liệu test khi reset hệ thống bằng `ClearThiSinhTable.java`
- Hỗ trợ kiểm tra dữ liệu nhập từ bảng quy đổi khi cần đối chiếu

**Việc cần làm kỹ:**
- Xác thực đăng nhập, phân quyền admin/user
- Khóa / mở khóa tài khoản
- CRUD điểm thi 3 phương thức: THPT / V-SAT / ĐGNL
- Import điểm thi
- Kiểm tra ràng buộc `phuongThuc`, `N1_CC`, `NL1`
- Kiểm tra dữ liệu nhập sai trước khi import mới
- Chạy tích hợp toàn bộ luồng dữ liệu
- Xác nhận hệ thống chạy đúng sau khi import dữ liệu mới
- Hỗ trợ các thành viên khác khi cần reset môi trường test

### 4.3. Minh - Ngành Tuyển Sinh + Cấu Hình Chỉ Tiêu
Minh làm nhóm dữ liệu ngành, chỉ tiêu và cấu hình tuyển sinh, đây là phần nền nghiệp vụ rất quan trọng.

**Phạm vi phụ trách:**
- `NganhGUI.java`
- `NganhBUS.java`
- `NganhDAO.java`
- `Nganh.java`
- Quản lý ngành / chỉ tiêu / điểm sàn / cờ xét tuyển
- Kiểm tra logic các trường `n_tohopgoc`, `n_chitieu`, `n_diemsan`, `n_diemtrungtuyen`

**Việc cần làm kỹ:**
- CRUD ngành tuyển sinh
- Quản lý chỉ tiêu, điểm sàn, cờ xét tuyển
- Chuẩn hóa dữ liệu tên ngành, mã ngành, các cờ phương thức xét tuyển
- Làm rõ đầu vào cho phần mapping ngành - tổ hợp

### 4.4. Khoa - Ngành - Tổ Hợp + Bảng Quy Đổi
Khoa nhận phần mapping và quy đổi, đúng với nhóm logic trung gian của hệ thống.

**Phạm vi phụ trách:**
- `NganhToHopGUI.java`
- `NganhToHopBUS.java`
- `NganhToHopDAO.java`
- `NganhToHop.java`
- `BangQuyDoiGUI.java`
- `BangQuyDoiBUS.java`
- `BangQuyDoiDAO.java`
- `BangQuyDoi.java`

**Việc cần làm kỹ:**
- Map ngành với tổ hợp
- Tính / lưu `dolech` theo tổ hợp gốc
- Quản lý bảng quy đổi theo phương thức
- Hỗ trợ logic quy đổi điểm giữa THPT, V-SAT, ĐGNL
- Kiểm tra tính nhất quán `d_phuongthuc`, `d_tohop`, `d_mon`, `d_phanvi`

### 4.5. Khánh - Nguyện Vọng + Xét Tuyển
Khánh phụ trách phần cuối chuỗi nghiệp vụ, nhưng không chỉ dừng ở nhập nguyện vọng mà còn cần ghép dữ liệu để ra kết quả.

**Phạm vi phụ trách:**
- `NguyenVongXetTuyenGUI.java`
- `NguyenVongXetTuyenBUS.java`
- `NguyenVongXetTuyenDAO.java`
- `NguyenVongXetTuyen.java`
- Đọc / nhập dữ liệu nguyện vọng từng thí sinh
- Gắn phương thức xét tuyển cho từng dòng nguyện vọng

**Việc cần làm kỹ:**
- Import nguyện vọng từng thí sinh
- Quản lý thứ tự nguyện vọng `nv_tt`
- Tính điểm xét tuyển cuối cùng
- Lưu trạng thái `nv_ketqua`
- Ghép điểm thi + điểm cộng + tổ hợp + phương thức
- Kiểm tra bản ghi trùng, thiếu CCCD, thiếu ngành

### 4.6. Huy - Dashboard + App Shell + Theme + Tích Hợp
Huy làm lớp khung tổng hợp, giúp các module gắn với nhau và dễ kiểm thử toàn hệ thống.

**Phạm vi phụ trách:**
- `DashboardGUI.java`
- `PhanMemTuyenSinh.java`
- `PhanMemTuyenSinhApp.java`
- `ModernTheme.java`
- `DiemCongXetTuyenGUI.java`
- `DiemCongXetTuyenBUS.java`
- `DiemCongXetTuyenDAO.java`
- `DiemCongXetTuyen.java`

**Việc cần làm kỹ:**
- Giao diện tổng quan / navigation
- Đồng bộ theme và trải nghiệm UI
- Phân quyền ẩn/khóa nút theo user
- Kiểm tra khả năng mở từng module từ app chính

## 5. Gợi Ý Cách Chia Việc Hợp Lý

### Nhóm theo luồng nghiệp vụ
- **Nhóm 1 - Dữ liệu gốc**: Tài, Minh
- **Nhóm 2 - Tài khoản, điểm và kiểm tra dữ liệu**: Thịnh
- **Nhóm 3 - Mapping và quy đổi**: Khoa
- **Nhóm 4 - Nguyện vọng và kết quả xét tuyển**: Khánh
- **Nhóm 5 - Tích hợp UI, nền ứng dụng và kiểm thử**: Huy

### Quan hệ bàn giao
- Tài xong thí sinh và tổ hợp môn -> Khánh dùng CCCD và mã tổ hợp để ghép nguyện vọng
- Thịnh xong điểm thi/điểm cộng -> Khánh dùng để tính kết quả
- Minh xong ngành/tổ hợp -> Khoa dùng để map quy đổi
- Khoa xong bảng quy đổi -> Khánh dùng để tính điểm cuối
- Thịnh kiểm thử end-to-end toàn hệ thống sau khi các module chính hoàn tất

## 6. Việc Nên Chốt Rõ Trước Khi Làm

1. Thống nhất quy ước mã phương thức: `PT2`, `PT3`, `PT4`, `DGNL`, `V-SAT`, `THPT`
2. Thống nhất khóa liên kết chính giữa các bảng là CCCD
3. Thống nhất cách xử lý dữ liệu `NaN` khi import từ txt/excel
4. Thống nhất module nào được phép sửa trực tiếp dữ liệu, module nào chỉ xem
5. Thống nhất phần xét tuyển cuối cùng có làm ngay trong app hay chỉ lưu dữ liệu trung gian

## 7. Nếu Làm Thêm Nền Tảng Web Sau Này

Nếu project tiến tới web, nên đi theo hướng tách dần phần nghiệp vụ khỏi Swing hiện tại thay vì viết lại từ đầu.

### Hướng triển khai khuyến nghị
- Giữ nguyên database và các entity / business rule đang có
- Tách logic chung ở `BUS` thành service dùng lại cho web
- Xây thêm backend API thay vì nối trực tiếp UI với DAO
- Web chỉ gọi API, không xử lý rule xét tuyển ở frontend
- Sau khi web ổn định, mới cân nhắc đồng bộ sang mobile

### Chia việc nếu mở rộng web
- **Backend/API**: 1-2 người
- **Web UI**: 1-2 người
- **Database / migration / tích hợp**: 1 người
- **Kiểm thử end-to-end và đồng bộ dữ liệu**: 1 người

### Cách đi an toàn nhất
1. Bước 1: Đóng gói nghiệp vụ hiện tại thành lớp dùng chung
2. Bước 2: Làm backend API cho người dùng, thí sinh, ngành, điểm thi, điểm cộng, nguyện vọng
3. Bước 3: Làm web admin trước, chưa cần mobile ngay
4. Bước 4: Khi web ổn định mới làm responsive cho sinh viên/thí sinh
5. Bước 5: Nếu cần mobile, dùng lại API của web, không viết logic xét tuyển lại

## 8. Kết Luận

Đây là cấu trúc phân việc cân bằng hơn với code hiện tại:
- `Tài`: thí sinh + chuẩn bị dữ liệu đầu vào
- `Thịnh`: tài khoản + điểm thi + cấu hình + test end-to-end + kiểm tra dữ liệu liên quan
- `Minh`: ngành tuyển sinh + chỉ tiêu
- `Khoa`: ngành - tổ hợp + bảng quy đổi
- `Khánh`: nguyện vọng và xét tuyển
- `Huy`: dashboard + app shell + theme + tích hợp + điểm cộng

Cách chia này giữ đúng phần mỗi người đang làm, giảm chồng chéo, và đảm bảo module nào cũng có đầu vào/đầu ra rõ ràng.
