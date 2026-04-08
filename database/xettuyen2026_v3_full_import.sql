-- MySQL dump (custom)
-- VERSION v3: full-import friendly schema for data/* files
-- Target DB: xettuyen2026

CREATE DATABASE IF NOT EXISTS xettuyen2026 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xettuyen2026;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;

-- =====================================================
-- 1) User management
-- =====================================================
DROP TABLE IF EXISTS xt_nguoidung;
CREATE TABLE xt_nguoidung (
  idnguoidung int NOT NULL AUTO_INCREMENT,
  taikhoan varchar(45) NOT NULL,
  matkhau varchar(255) NOT NULL,
  hoten varchar(100) DEFAULT NULL,
  email varchar(100) DEFAULT NULL,
  dienthoai varchar(20) DEFAULT NULL,
  phanquyen varchar(45) NOT NULL DEFAULT 'user',
  trangthaihoatdong tinyint(1) DEFAULT 1,
  ngaytao datetime DEFAULT CURRENT_TIMESTAMP,
  ngaysua datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (idnguoidung),
  UNIQUE KEY taikhoan_UNIQUE (taikhoan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO xt_nguoidung
(taikhoan, matkhau, hoten, email, dienthoai, phanquyen, trangthaihoatdong)
VALUES
('admin', 'admin123', 'Administrator', 'admin@sgu.edu.vn', '0123456789', 'admin', 1),
('user01', 'user123', 'User 01', 'user01@sgu.edu.vn', '0987654321', 'user', 1);

-- =====================================================
-- 2) Candidate profile (expanded for full raw import)
-- =====================================================
DROP TABLE IF EXISTS xt_thisinhxettuyen25;
CREATE TABLE xt_thisinhxettuyen25 (
  idthisinh int NOT NULL AUTO_INCREMENT,
  cccd varchar(20) DEFAULT NULL,
  sobaodanh varchar(45) DEFAULT NULL,
  ho varchar(100) DEFAULT NULL,
  ten varchar(100) DEFAULT NULL,
  ngay_sinh varchar(45) DEFAULT NULL,
  dien_thoai varchar(20) DEFAULT NULL,
  password varchar(100) DEFAULT NULL,
  gioi_tinh varchar(10) DEFAULT NULL,
  email varchar(100) DEFAULT NULL,
  noi_sinh varchar(100) DEFAULT NULL,
  updated_at date DEFAULT NULL,
  doi_tuong varchar(45) DEFAULT NULL,
  khu_vuc varchar(45) DEFAULT NULL,
  dan_toc varchar(100) DEFAULT NULL,
  ma_dan_toc varchar(20) DEFAULT NULL,
  chuong_trinh_hoc varchar(50) DEFAULT NULL,
  ma_mon_nn varchar(20) DEFAULT NULL,
  PRIMARY KEY (idthisinh),
  UNIQUE KEY cccd_UNIQUE (cccd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3) Candidate exam scores (expanded for Ds thi sinh full columns)
-- =====================================================
DROP TABLE IF EXISTS xt_diemthixettuyen;
CREATE TABLE xt_diemthixettuyen (
  iddiemthi int NOT NULL AUTO_INCREMENT,
  cccd varchar(20) NOT NULL,
  sobaodanh varchar(45) DEFAULT NULL,
  d_phuongthuc varchar(10) DEFAULT NULL,
  `TO` decimal(8,2) DEFAULT 0.00,
  `LI` decimal(8,2) DEFAULT 0.00,
  `HO` decimal(8,2) DEFAULT 0.00,
  `SI` decimal(8,2) DEFAULT 0.00,
  `SU` decimal(8,2) DEFAULT 0.00,
  `DI` decimal(8,2) DEFAULT 0.00,
  `VA` decimal(8,2) DEFAULT 0.00,
  GDCD decimal(8,2) DEFAULT 0.00,
  N1_THI decimal(8,2) DEFAULT NULL COMMENT 'Diem thi goc mon Ngoai ngu 1',
  N1_CC decimal(8,2) DEFAULT 0.00 COMMENT 'max(N1_THI, diem quy doi chung chi)',
  `CNCN` decimal(8,2) DEFAULT 0.00,
  `CNNN` decimal(8,2) DEFAULT 0.00,
  `TI` decimal(8,2) DEFAULT 0.00,
  `KTPL` decimal(8,2) DEFAULT 0.00,
  NL1 decimal(8,2) DEFAULT NULL COMMENT 'Diem DGNL',
  NK1 decimal(8,2) DEFAULT NULL,
  NK2 decimal(8,2) DEFAULT NULL,
  NK3 decimal(8,2) DEFAULT NULL,
  NK4 decimal(8,2) DEFAULT NULL,
  NK5 decimal(8,2) DEFAULT NULL,
  NK6 decimal(8,2) DEFAULT NULL,
  NK7 decimal(8,2) DEFAULT NULL,
  NK8 decimal(8,2) DEFAULT NULL,
  NK9 decimal(8,2) DEFAULT NULL,
  NK10 decimal(8,2) DEFAULT NULL,
  diem_xet_tot_nghiep decimal(8,2) DEFAULT NULL,
  PRIMARY KEY (iddiemthi),
  UNIQUE KEY cccd_UNIQUE (cccd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4) Programs/majors
-- =====================================================
DROP TABLE IF EXISTS xt_nganh;
CREATE TABLE xt_nganh (
  idnganh int NOT NULL AUTO_INCREMENT,
  manganh varchar(45) NOT NULL,
  tennganh varchar(150) NOT NULL,
  n_tohopgoc varchar(10) DEFAULT NULL,
  n_chitieu int NOT NULL DEFAULT 0,
  n_diemsan decimal(10,2) DEFAULT NULL,
  n_diemtrungtuyen decimal(10,2) DEFAULT NULL,
  n_tuyenthang varchar(1) DEFAULT NULL,
  n_dgnl varchar(1) DEFAULT NULL,
  n_thpt varchar(1) DEFAULT NULL,
  n_vsat varchar(1) DEFAULT NULL,
  sl_xtt int DEFAULT NULL,
  sl_dgnl int DEFAULT NULL,
  sl_vsat int DEFAULT NULL,
  sl_thpt varchar(45) DEFAULT NULL,
  PRIMARY KEY (idnganh),
  UNIQUE KEY manganh_UNIQUE (manganh)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5) Subject combinations
-- =====================================================
DROP TABLE IF EXISTS xt_tohop_monthi;
CREATE TABLE xt_tohop_monthi (
  idtohop int NOT NULL AUTO_INCREMENT,
  matohop varchar(45) NOT NULL,
  mon1 varchar(10) NOT NULL,
  mon2 varchar(10) NOT NULL,
  mon3 varchar(10) NOT NULL,
  tentohop varchar(150) DEFAULT NULL,
  PRIMARY KEY (idtohop),
  UNIQUE KEY matohop_UNIQUE (matohop)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6) Major-combination mapping (expanded for full import columns)
-- =====================================================
DROP TABLE IF EXISTS xt_nganh_tohop;
CREATE TABLE xt_nganh_tohop (
  id int NOT NULL AUTO_INCREMENT,
  manganh varchar(45) NOT NULL,
  matohop varchar(45) NOT NULL,
  ten_nganh_chuan varchar(255) DEFAULT NULL,
  ten_to_hop varchar(150) DEFAULT NULL,
  la_tohop_goc varchar(20) DEFAULT NULL,
  th_mon1 varchar(10) DEFAULT NULL,
  hsmon1 tinyint DEFAULT NULL,
  th_mon2 varchar(10) DEFAULT NULL,
  hsmon2 tinyint DEFAULT NULL,
  th_mon3 varchar(10) DEFAULT NULL,
  hsmon3 tinyint DEFAULT NULL,
  tb_keys varchar(80) DEFAULT NULL COMMENT 'manganh_matohop',
  `N1` tinyint(1) DEFAULT NULL,
  `TO` tinyint(1) DEFAULT NULL,
  `LI` tinyint(1) DEFAULT NULL,
  `HO` tinyint(1) DEFAULT NULL,
  `SI` tinyint(1) DEFAULT NULL,
  `VA` tinyint(1) DEFAULT NULL,
  `SU` tinyint(1) DEFAULT NULL,
  `DI` tinyint(1) DEFAULT NULL,
  `TI` tinyint(1) DEFAULT NULL,
  `KHAC` tinyint(1) DEFAULT NULL,
  `KTPL` tinyint(1) DEFAULT NULL,
  `CNCN` tinyint(1) DEFAULT NULL,
  `CNNN` tinyint(1) DEFAULT NULL,
  dolech decimal(6,2) DEFAULT 0.00,
  PRIMARY KEY (id),
  UNIQUE KEY key_UNIQUE (tb_keys)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7) Bonus points / priority points (expanded for source metadata)
-- =====================================================
DROP TABLE IF EXISTS xt_diemcongxetuyen;
CREATE TABLE xt_diemcongxetuyen (
  iddiemcong int unsigned NOT NULL AUTO_INCREMENT,
  ts_cccd varchar(45) NOT NULL,
  manganh varchar(45) DEFAULT NULL,
  matohop varchar(20) DEFAULT NULL,
  phuongthuc varchar(45) DEFAULT NULL,
  diemCC decimal(6,2) DEFAULT NULL,
  diemUtxt decimal(6,2) DEFAULT NULL,
  diemTong decimal(6,2) DEFAULT 0.00,
  ghichu text,
  dc_keys varchar(45) NOT NULL,
  cap_giai varchar(50) DEFAULT NULL,
  doi_tuong_giai varchar(100) DEFAULT NULL,
  ma_mon_giai varchar(20) DEFAULT NULL,
  loai_giai varchar(100) DEFAULT NULL,
  diem_cong_mon_giai decimal(6,2) DEFAULT NULL,
  diem_cong_khong_mon decimal(6,2) DEFAULT NULL,
  co_chung_chi tinyint(1) DEFAULT NULL,
  chung_chi_ngoai_ngu varchar(150) DEFAULT NULL,
  diem_chung_chi varchar(50) DEFAULT NULL,
  diem_quy_doi_chung_chi decimal(6,2) DEFAULT NULL,
  PRIMARY KEY (iddiemcong),
  UNIQUE KEY dc_keys_UNIQUE (dc_keys)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8) Aspirations / admissions decisions (expanded for raw wish-list import)
-- =====================================================
DROP TABLE IF EXISTS xt_nguyenvongxettuyen;
CREATE TABLE xt_nguyenvongxettuyen (
  idnv int NOT NULL AUTO_INCREMENT,
  nn_cccd varchar(45) NOT NULL,
  nv_tt int NOT NULL,
  nv_matruong varchar(20) DEFAULT NULL,
  nv_tentruong varchar(255) DEFAULT NULL,
  nv_manganh varchar(45) NOT NULL,
  nv_tenmanganh varchar(255) DEFAULT NULL,
  nv_tuyenthang varchar(100) DEFAULT NULL,
  diem_thxt decimal(10,5) DEFAULT NULL COMMENT 'Da cong diem mon chinh',
  diem_utqd decimal(10,5) DEFAULT NULL COMMENT 'Diem UTQD theo to hop',
  diem_cong decimal(6,2) DEFAULT NULL,
  diem_xettuyen decimal(10,5) DEFAULT NULL COMMENT 'Da cong diem uu tien',
  nv_ketqua varchar(45) DEFAULT NULL,
  nv_keys varchar(45) DEFAULT NULL,
  tt_phuongthuc varchar(45) DEFAULT NULL,
  tt_thm varchar(45) DEFAULT NULL,
  PRIMARY KEY (idnv),
  UNIQUE KEY nv_keys_UNIQUE (nv_keys)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 9) Conversion table (kept, ready for percentile mapping)
-- =====================================================
DROP TABLE IF EXISTS xt_bangquydoi;
CREATE TABLE xt_bangquydoi (
  idqd int NOT NULL AUTO_INCREMENT,
  d_phuongthuc varchar(45) DEFAULT NULL,
  d_tohop varchar(45) DEFAULT NULL,
  d_mon varchar(45) DEFAULT NULL,
  d_diema decimal(8,2) DEFAULT NULL,
  d_diemb decimal(8,2) DEFAULT NULL,
  d_diemc decimal(8,2) DEFAULT NULL,
  d_diemd decimal(8,2) DEFAULT NULL,
  d_maquydoi varchar(80) DEFAULT NULL,
  d_phanvi varchar(45) DEFAULT NULL,
  PRIMARY KEY (idqd),
  UNIQUE KEY d_maquydoi_UNIQUE (d_maquydoi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET UNIQUE_CHECKS = 1;
SET FOREIGN_KEY_CHECKS = 1;
