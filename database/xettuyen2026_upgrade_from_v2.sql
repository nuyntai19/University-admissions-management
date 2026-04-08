-- Upgrade script: v2 -> full-import schema (non-destructive)
-- MySQL 8.0+

USE xettuyen2026;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1) Ensure user table exists (v2 should already have)
-- =====================================================
CREATE TABLE IF NOT EXISTS xt_nguoidung (
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

-- =====================================================
-- 2) xt_diemthixettuyen: add missing score columns
-- Source gaps: GDCD, NK3..NK10, diem_xet_tot_nghiep
-- =====================================================
ALTER TABLE xt_diemthixettuyen
  ADD COLUMN IF NOT EXISTS GDCD decimal(8,2) DEFAULT 0.00 AFTER VA,
  ADD COLUMN IF NOT EXISTS NK3 decimal(8,2) DEFAULT NULL AFTER NK2,
  ADD COLUMN IF NOT EXISTS NK4 decimal(8,2) DEFAULT NULL AFTER NK3,
  ADD COLUMN IF NOT EXISTS NK5 decimal(8,2) DEFAULT NULL AFTER NK4,
  ADD COLUMN IF NOT EXISTS NK6 decimal(8,2) DEFAULT NULL AFTER NK5,
  ADD COLUMN IF NOT EXISTS NK7 decimal(8,2) DEFAULT NULL AFTER NK6,
  ADD COLUMN IF NOT EXISTS NK8 decimal(8,2) DEFAULT NULL AFTER NK7,
  ADD COLUMN IF NOT EXISTS NK9 decimal(8,2) DEFAULT NULL AFTER NK8,
  ADD COLUMN IF NOT EXISTS NK10 decimal(8,2) DEFAULT NULL AFTER NK9,
  ADD COLUMN IF NOT EXISTS diem_xet_tot_nghiep decimal(8,2) DEFAULT NULL AFTER NK10;

-- =====================================================
-- 3) xt_thisinhxettuyen25: add missing profile columns
-- Source gaps: dan_toc, ma_dan_toc, chuong_trinh_hoc, ma_mon_nn
-- =====================================================
ALTER TABLE xt_thisinhxettuyen25
  ADD COLUMN IF NOT EXISTS dan_toc varchar(100) DEFAULT NULL AFTER khu_vuc,
  ADD COLUMN IF NOT EXISTS ma_dan_toc varchar(20) DEFAULT NULL AFTER dan_toc,
  ADD COLUMN IF NOT EXISTS chuong_trinh_hoc varchar(50) DEFAULT NULL AFTER ma_dan_toc,
  ADD COLUMN IF NOT EXISTS ma_mon_nn varchar(20) DEFAULT NULL AFTER chuong_trinh_hoc,
  MODIFY COLUMN noi_sinh varchar(100) DEFAULT NULL;

-- =====================================================
-- 4) xt_nguyenvongxettuyen: add detailed wish-list columns
-- Source gaps: ma truong, ten truong, ten ma xet tuyen, nguyen vong tuyen thang
-- =====================================================
ALTER TABLE xt_nguyenvongxettuyen
  ADD COLUMN IF NOT EXISTS nv_matruong varchar(20) DEFAULT NULL AFTER nv_tt,
  ADD COLUMN IF NOT EXISTS nv_tentruong varchar(255) DEFAULT NULL AFTER nv_matruong,
  ADD COLUMN IF NOT EXISTS nv_tenmanganh varchar(255) DEFAULT NULL AFTER nv_manganh,
  ADD COLUMN IF NOT EXISTS nv_tuyenthang varchar(100) DEFAULT NULL AFTER nv_tenmanganh,
  MODIFY COLUMN nv_keys varchar(120) DEFAULT NULL;

-- =====================================================
-- 5) xt_diemcongxetuyen: add source metadata columns
-- Source gaps: cap, doi tuong, ma mon, loai giai, co chung chi...
-- =====================================================
ALTER TABLE xt_diemcongxetuyen
  ADD COLUMN IF NOT EXISTS cap_giai varchar(50) DEFAULT NULL AFTER ghichu,
  ADD COLUMN IF NOT EXISTS doi_tuong_giai varchar(100) DEFAULT NULL AFTER cap_giai,
  ADD COLUMN IF NOT EXISTS ma_mon_giai varchar(20) DEFAULT NULL AFTER doi_tuong_giai,
  ADD COLUMN IF NOT EXISTS loai_giai varchar(100) DEFAULT NULL AFTER ma_mon_giai,
  ADD COLUMN IF NOT EXISTS diem_cong_mon_giai decimal(6,2) DEFAULT NULL AFTER loai_giai,
  ADD COLUMN IF NOT EXISTS diem_cong_khong_mon decimal(6,2) DEFAULT NULL AFTER diem_cong_mon_giai,
  ADD COLUMN IF NOT EXISTS co_chung_chi tinyint(1) DEFAULT NULL AFTER diem_cong_khong_mon,
  ADD COLUMN IF NOT EXISTS chung_chi_ngoai_ngu varchar(150) DEFAULT NULL AFTER co_chung_chi,
  ADD COLUMN IF NOT EXISTS diem_chung_chi varchar(50) DEFAULT NULL AFTER chung_chi_ngoai_ngu,
  ADD COLUMN IF NOT EXISTS diem_quy_doi_chung_chi decimal(6,2) DEFAULT NULL AFTER diem_chung_chi,
  MODIFY COLUMN manganh varchar(45) DEFAULT NULL,
  MODIFY COLUMN matohop varchar(20) DEFAULT NULL,
  MODIFY COLUMN dc_keys varchar(80) NOT NULL;

-- =====================================================
-- 6) xt_nganh_tohop: add descriptive columns for raw import
-- Source gaps: ten_nganh_chuan, ten_to_hop, co goi la to hop goc
-- =====================================================
ALTER TABLE xt_nganh_tohop
  ADD COLUMN IF NOT EXISTS ten_nganh_chuan varchar(255) DEFAULT NULL AFTER matohop,
  ADD COLUMN IF NOT EXISTS ten_to_hop varchar(150) DEFAULT NULL AFTER ten_nganh_chuan,
  ADD COLUMN IF NOT EXISTS la_tohop_goc varchar(20) DEFAULT NULL AFTER ten_to_hop,
  ADD COLUMN IF NOT EXISTS CNCN tinyint(1) DEFAULT NULL AFTER KTPL,
  ADD COLUMN IF NOT EXISTS CNNN tinyint(1) DEFAULT NULL AFTER CNCN,
  MODIFY COLUMN tb_keys varchar(80) DEFAULT NULL,
  MODIFY COLUMN th_mon3 varchar(10) DEFAULT NULL;

-- =====================================================
-- 7) Optional quality-of-life widening for conversion table
-- =====================================================
ALTER TABLE xt_bangquydoi
  MODIFY COLUMN d_diema decimal(8,2) DEFAULT NULL,
  MODIFY COLUMN d_diemb decimal(8,2) DEFAULT NULL,
  MODIFY COLUMN d_diemc decimal(8,2) DEFAULT NULL,
  MODIFY COLUMN d_diemd decimal(8,2) DEFAULT NULL,
  MODIFY COLUMN d_maquydoi varchar(80) DEFAULT NULL;

SET FOREIGN_KEY_CHECKS = 1;

-- Done.
