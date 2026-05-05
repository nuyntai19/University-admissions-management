# Hướng dẫn cập nhật Database từ v3 sang v4 (Giữ nguyên dữ liệu)

## 📋 Tổng quan

Script migration này sẽ cập nhật cấu trúc database từ v3 Full Import sang v4 Structure Only **mà vẫn giữ nguyên toàn bộ dữ liệu**.

## 🔧 Những thay đổi sẽ được thực hiện

### 1️⃣ Bảng `xt_diemcongxetuyen`
- **Thay đổi**: Tăng độ chính xác decimal
  - `diem_cong_mon_giai`: `decimal(6,2)` → `decimal(38,2)`
  - `diem_cong_khong_mon`: `decimal(6,2)` → `decimal(38,2)`

### 2️⃣ Bảng `xt_nganh_tohop`
- **Thay đổi**: Thay đổi kiểu dữ liệu + Thêm cột mới
  - `CNCN`: `tinyint(1)` → `int`
  - `CNNN`: `tinyint(1)` → `int`
  - **Thêm cột mới**: `GDCD int DEFAULT NULL`

### 3️⃣ Bảng `xt_nguyenvongxettuyen`
- **Thay đổi 1**: Tăng độ chính xác decimal
  - `diem_thxt`: `decimal(10,5)` → `decimal(38,2)`
  - `diem_utqd`: `decimal(10,5)` → `decimal(38,2)`
  - `diem_cong`: `decimal(6,2)` → `decimal(38,2)`
  - `diem_xettuyen`: `decimal(10,5)` → `decimal(38,2)`

- **Thay đổi 2**: Sắp xếp lại thứ tự cột
  - **V3**: `nn_cccd` ở vị trí **đầu** (cột thứ 2)
  - **V4**: `nn_cccd` ở vị trí **cuối** (cột cuối cùng)

### 4️⃣ Bảng `xt_diemthixettuyen`
- **Thay đổi**: Thêm unique key bổ sung
  - Thêm constraint: `UKfhm3o5xso39x2e1ts1ogmn89b` (duplicate của cccd_phuongthuc_UNIQUE)

## 🚀 Hướng dẫn thực hiện

### **Cách 1: Sử dụng MySQL Command Line**

```bash
# 1. Kết nối tới MySQL
mysql -u root -p

# 2. Chạy script migration
SOURCE d:\sinh vien\PHAN MEM PHAN LOP\project\University-admissions-management\database\migrate_v3_to_v4.sql;

# 3. Kiểm tra kết quả
SHOW TABLES;
DESCRIBE xt_nguyenvongxettuyen;
```

### **Cách 2: Sử dụng PhpMyAdmin**

1. Đăng nhập vào PhpMyAdmin
2. Chọn database `xettuyen2026`
3. Vào tab **SQL**
4. Copy toàn bộ nội dung file `migrate_v3_to_v4.sql`
5. Dán vào khung SQL và click **Thực thi (Execute)**
6. Chờ hoàn tất và kiểm tra kết quả

### **Cách 3: Sử dụng MySQL Workbench**

1. Mở MySQL Workbench
2. Kết nối tới server
3. File → Open SQL Script → Chọn file `migrate_v3_to_v4.sql`
4. Click **Execute** (Ctrl + Shift + Enter)
5. Kiểm tra **Output** tab để xem kết quả

## ✅ Kiểm chứng kết quả

Script đã bao gồm phần kiểm chứng tự động:

```sql
-- Sẽ hiển thị cấu trúc bảng mới
DESCRIBE `xt_diemcongxetuyen`;
DESCRIBE `xt_nganh_tohop`;
DESCRIBE `xt_nguyenvongxettuyen`;
DESCRIBE `xt_diemthixettuyen`;

-- Sẽ kiểm tra số lượng dữ liệu (không thay đổi)
SELECT table_name, total_records FROM ...
```

**Kết quả mong đợi**:
- Tất cả bảng vẫn có dữ liệu giống hệt như trước
- Cấu trúc cột được cập nhật theo v4
- Không có lỗi hoặc ảnh hưởng dữ liệu

## ⚠️ Lưu ý quan trọng

1. **Backup dữ liệu trước**: 
   ```sql
   -- Tạo bảng backup
   CREATE TABLE `xt_nguyenvongxettuyen_backup` LIKE `xt_nguyenvongxettuyen`;
   INSERT INTO `xt_nguyenvongxettuyen_backup` SELECT * FROM `xt_nguyenvongxettuyen`;
   ```

2. **Kiểm tra kỹ lưỡng**:
   - Chạy script trên database test trước
   - Kiểm tra dữ liệu sau migration

3. **Nếu cần rollback**:
   ```sql
   -- Restore từ backup
   TRUNCATE TABLE `xt_nguyenvongxettuyen`;
   INSERT INTO `xt_nguyenvongxettuyen` SELECT * FROM `xt_nguyenvongxettuyen_backup`;
   ```

4. **COLLATE tùy chọn**:
   - Script hiện tại không bắt buộc cập nhật COLLATE cho tất cả bảng
   - Nếu muốn chuẩn hóa hoàn toàn, bỏ comment dòng:
   ```sql
   ALTER TABLE `xt_bangquydoi` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

## 📊 So sánh chi tiết

| Bảng | Thay đổi | Tác động |
|------|----------|---------|
| `xt_diemcongxetuyen` | Tăng độ chính xác decimal | Dữ liệu tự động chuyển đổi |
| `xt_nganh_tohop` | +1 cột, thay đổi kiểu | Dữ liệu cũ giữ nguyên, cột mới = NULL |
| `xt_nguyenvongxettuyen` | Thay đổi thứ tự + decimal | Dữ liệu giữ nguyên nhưng sắp xếp lại |
| `xt_diemthixettuyen` | Thêm unique key | Không ảnh hưởng dữ liệu |

## 🔄 Quy trình an toàn

```
1. Backup dữ liệu hiện tại
       ↓
2. Chạy script migration trên database test
       ↓
3. Kiểm chứng dữ liệu
       ↓
4. So sánh kết quả với v4 chuẩn
       ↓
5. Chạy trên production
       ↓
6. Kiểm tra toàn diện trước khi commit
```

---

**Ngày tạo**: 2026-05-04  
**Phiên bản**: v1.0  
**Trạng thái**: Sẵn sàng sử dụng
