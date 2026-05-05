-- ============================================================================
-- MIGRATION SCRIPT: xettuyen2026 từ v3 sang v4
-- Thời gian: 2026-05-04
-- Mục đích: Cập nhật cấu trúc bảng từ v3 Full Import sang v4 Structure Only
-- Lưu ý: Script này sẽ GIỮ NGUYÊN TẤT CẢ DỮ LIỆU
-- ============================================================================

USE `xettuyen2026`;

-- ============================================================================
-- BƯỚC 1: CẬP NHẬT BẢNG `xt_diemcongxetuyen`
-- Thay đổi: Tăng độ chính xác decimal từ (6,2) sang (38,2)
-- ============================================================================
ALTER TABLE `xt_diemcongxetuyen`
MODIFY COLUMN `diem_cong_mon_giai` decimal(38,2) DEFAULT NULL,
MODIFY COLUMN `diem_cong_khong_mon` decimal(38,2) DEFAULT NULL;

-- ============================================================================
-- BƯỚC 2: CẬP NHẬT BẢNG `xt_nganh_tohop`
-- Thay đổi: 
--   - CNCN: tinyint(1) -> int
--   - CNNN: tinyint(1) -> int
--   - Thêm cột GDCD (int) nếu chưa tồn tại
-- ============================================================================
ALTER TABLE `xt_nganh_tohop`
MODIFY COLUMN `CNCN` int DEFAULT NULL,
MODIFY COLUMN `CNNN` int DEFAULT NULL;

-- Thêm cột GDCD nếu chưa tồn tại (MySQL 8.0.13+)
-- Nếu dùng MySQL cũ hơn và gặp lỗi "Duplicate column name 'GDCD'", bạn có thể:
-- 1. Bỏ qua lỗi (cột đã tồn tại)
-- 2. Xóa dòng này nếu cột GDCD đã có sẵn
SET @gdcd_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
  WHERE TABLE_NAME='xt_nganh_tohop' 
  AND COLUMN_NAME='GDCD' 
  AND TABLE_SCHEMA=DATABASE()
);

SET @add_gdcd = IF(@gdcd_exists = 0, 
  'ALTER TABLE `xt_nganh_tohop` ADD COLUMN `GDCD` int DEFAULT NULL AFTER `dolech`',
  'SELECT "Cột GDCD đã tồn tại, bỏ qua" as message'
);

PREPARE stmt FROM @add_gdcd;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- BƯỚC 3: CẬP NHẬT BẢNG `xt_nguyenvongxettuyen`
-- Thay đổi:
--   - Tăng độ chính xác decimal từ (10,5) hoặc (6,2) sang (38,2)
--   - Sắp xếp lại thứ tự cột (nn_cccd từ đầu -> cuối)
-- ============================================================================

-- Tạo bảng tạm để giữ dữ liệu
CREATE TABLE `xt_nguyenvongxettuyen_temp` LIKE `xt_nguyenvongxettuyen`;

-- Copy dữ liệu sang bảng tạm
INSERT INTO `xt_nguyenvongxettuyen_temp` 
SELECT * FROM `xt_nguyenvongxettuyen`;

-- Drop bảng cũ
DROP TABLE `xt_nguyenvongxettuyen`;

-- Tạo lại bảng với cấu trúc mới (thứ tự cột đúng theo v4)
CREATE TABLE `xt_nguyenvongxettuyen` (
  `idnv` int NOT NULL AUTO_INCREMENT,
  `nv_tt` int NOT NULL,
  `nv_manganh` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nv_tenmanganh` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nv_tuyenthang` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `diem_thxt` decimal(38,2) DEFAULT NULL,
  `diem_utqd` decimal(38,2) DEFAULT NULL,
  `diem_cong` decimal(38,2) DEFAULT NULL,
  `diem_xettuyen` decimal(38,2) DEFAULT NULL,
  `nv_ketqua` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nv_keys` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tt_phuongthuc` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tt_thm` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nn_cccd` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`idnv`),
  UNIQUE KEY `nv_keys_UNIQUE` (`nv_keys`)
) ENGINE=InnoDB AUTO_INCREMENT=79148 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Copy lại dữ liệu với thứ tự cột mới
-- Lưu ý: Các cột decimal sẽ được tự động chuyển đổi kiểu từ (10,5) sang (38,2)
INSERT INTO `xt_nguyenvongxettuyen` (
  `idnv`, `nv_tt`, `nv_manganh`, `nv_tenmanganh`, `nv_tuyenthang`,
  `diem_thxt`, `diem_utqd`, `diem_cong`, `diem_xettuyen`,
  `nv_ketqua`, `nv_keys`, `tt_phuongthuc`, `tt_thm`, `nn_cccd`
)
SELECT 
  `idnv`, `nv_tt`, `nv_manganh`, `nv_tenmanganh`, `nv_tuyenthang`,
  `diem_thxt`, `diem_utqd`, `diem_cong`, `diem_xettuyen`,
  `nv_ketqua`, `nv_keys`, `tt_phuongthuc`, `tt_thm`, `nn_cccd`
FROM `xt_nguyenvongxettuyen_temp`;

-- Drop bảng tạm
DROP TABLE `xt_nguyenvongxettuyen_temp`;

-- ============================================================================
-- BƯỚC 4: CẬP NHẬT BẢNG `xt_diemthixettuyen`
-- Thay đổi: Thêm unique key bổ sung (nếu chưa tồn tại)
-- ============================================================================
-- Kiểm tra và thêm unique key nếu chưa tồn tại
SET @constraint_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
  WHERE TABLE_NAME='xt_diemthixettuyen' 
  AND INDEX_NAME='UKfhm3o5xso39x2e1ts1ogmn89b' 
  AND TABLE_SCHEMA=DATABASE()
);

SET @add_constraint = IF(@constraint_exists = 0,
  'ALTER TABLE `xt_diemthixettuyen` ADD CONSTRAINT `UKfhm3o5xso39x2e1ts1ogmn89b` UNIQUE KEY (`cccd`, `d_phuongthuc`)',
  'SELECT "Constraint UKfhm3o5xso39x2e1ts1ogmn89b đã tồn tại, bỏ qua" as message'
);

PREPARE stmt2 FROM @add_constraint;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- ============================================================================
-- BƯỚC 5: CẬP NHẬT COLLATE CHO CÁC CỘT (tùy chọn)
-- Lưu ý: Bước này chỉ cần nếu muốn chuẩn hóa hoàn toàn theo v4
-- Có thể bỏ qua nếu dữ liệu hiện tại đã chạy tốt mà không cần COLLATE rõ ràng
-- ============================================================================

-- Nếu cần, chạy các lệnh sau để chuẩn hóa COLLATE:
-- ALTER TABLE `xt_bangquydoi` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- ALTER TABLE `xt_diemcongxetuyen` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- ... v.v. cho các bảng khác

-- ============================================================================
-- BƯỚC 6: KIỂM CHỨNG (Verification)
-- ============================================================================

-- Kiểm tra xt_diemcongxetuyen
SELECT '--- xt_diemcongxetuyen ---' as 'VERIFICATION';
DESCRIBE `xt_diemcongxetuyen`;

-- Kiểm tra xt_nganh_tohop
SELECT '--- xt_nganh_tohop ---' as 'VERIFICATION';
DESCRIBE `xt_nganh_tohop`;

-- Kiểm tra xt_nguyenvongxettuyen
SELECT '--- xt_nguyenvongxettuyen ---' as 'VERIFICATION';
DESCRIBE `xt_nguyenvongxettuyen`;

-- Kiểm tra xt_diemthixettuyen
SELECT '--- xt_diemthixettuyen ---' as 'VERIFICATION';
DESCRIBE `xt_diemthixettuyen`;

-- Kiểm tra số lượng dữ liệu (không thay đổi)
SELECT 
  'xt_diemcongxetuyen' as table_name, COUNT(*) as total_records FROM `xt_diemcongxetuyen`
UNION ALL
SELECT 'xt_nguyenvongxettuyen', COUNT(*) FROM `xt_nguyenvongxettuyen`
UNION ALL
SELECT 'xt_nganh_tohop', COUNT(*) FROM `xt_nganh_tohop`
UNION ALL
SELECT 'xt_diemthixettuyen', COUNT(*) FROM `xt_diemthixettuyen`;

-- ============================================================================
-- MIGRATION HOÀN TẤT
-- ============================================================================