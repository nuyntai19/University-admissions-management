-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th4 14, 2026 lúc 08:41 AM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

CREATE DATABASE IF NOT EXISTS `xettuyen2026` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xettuyen2026`;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `xettuyen2026`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `xt_bangquydoi`
--

CREATE TABLE `xt_bangquydoi` (
  `idqd` int(11) NOT NULL,
  `d_phuongthuc` varchar(45) DEFAULT NULL,
  `d_tohop` varchar(45) DEFAULT NULL,
  `d_mon` varchar(45) DEFAULT NULL,
  `d_diema` decimal(38,2) DEFAULT NULL,
  `d_diemb` decimal(38,2) DEFAULT NULL,
  `d_diemc` decimal(38,2) DEFAULT NULL,
  `d_diemd` decimal(38,2) DEFAULT NULL,
  `d_maquydoi` varchar(80) DEFAULT NULL,
  `d_phanvi` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `xt_diemcongxetuyen`
--

CREATE TABLE `xt_diemcongxetuyen` (
  `iddiemcong` int(10) UNSIGNED NOT NULL,
  `ts_cccd` varchar(45) NOT NULL,
  `manganh` varchar(45) DEFAULT NULL,
  `matohop` varchar(20) DEFAULT NULL,
  `phuongthuc` varchar(45) DEFAULT NULL,
  `diemCC` decimal(38,2) DEFAULT NULL,
  `diemUtxt` decimal(38,2) DEFAULT NULL,
  `diemTong` decimal(38,2) DEFAULT NULL,
  `ghichu` text DEFAULT NULL,
  `dc_keys` varchar(45) NOT NULL,
  `cap_giai` varchar(50) DEFAULT NULL,
  `doi_tuong_giai` varchar(100) DEFAULT NULL,
  `ma_mon_giai` varchar(20) DEFAULT NULL,
  `loai_giai` varchar(100) DEFAULT NULL,
  `diem_cong_mon_giai` decimal(6,2) DEFAULT NULL,
  `diem_cong_khong_mon` decimal(6,2) DEFAULT NULL,
  `co_chung_chi` tinyint(1) DEFAULT NULL,
  `chung_chi_ngoai_ngu` varchar(150) DEFAULT NULL,
  `diem_chung_chi` varchar(50) DEFAULT NULL,
  `diem_quy_doi_chung_chi` decimal(6,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `xt_diemthixettuyen`
--

CREATE TABLE `xt_diemthixettuyen` (
  `iddiemthi` int(11) NOT NULL,
  `cccd` varchar(20) NOT NULL,
  `sobaodanh` varchar(45) DEFAULT NULL,
  `d_phuongthuc` varchar(10) DEFAULT NULL,
  `TO` decimal(38,2) DEFAULT NULL,
  `LI` decimal(38,2) DEFAULT NULL,
  `HO` decimal(38,2) DEFAULT NULL,
  `SI` decimal(38,2) DEFAULT NULL,
  `SU` decimal(38,2) DEFAULT NULL,
  `DI` decimal(38,2) DEFAULT NULL,
  `VA` decimal(38,2) DEFAULT NULL,
  `GDCD` decimal(38,2) DEFAULT NULL,
  `N1_THI` decimal(38,2) DEFAULT NULL,
  `N1_CC` decimal(38,2) DEFAULT NULL,
  `CNCN` decimal(38,2) DEFAULT NULL,
  `CNNN` decimal(38,2) DEFAULT NULL,
  `TI` decimal(38,2) DEFAULT NULL,
  `KTPL` decimal(38,2) DEFAULT NULL,
  `NL1` decimal(38,2) DEFAULT NULL,
  `NK1` decimal(38,2) DEFAULT NULL,
  `NK2` decimal(38,2) DEFAULT NULL,
  `NK3` decimal(38,2) DEFAULT NULL,
  `NK4` decimal(38,2) DEFAULT NULL,
  `NK5` decimal(38,2) DEFAULT NULL,
  `NK6` decimal(38,2) DEFAULT NULL,
  `NK7` decimal(38,2) DEFAULT NULL,
  `NK8` decimal(38,2) DEFAULT NULL,
  `NK9` decimal(38,2) DEFAULT NULL,
  `NK10` decimal(38,2) DEFAULT NULL,
  `diem_xet_tot_nghiep` decimal(38,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `xt_nganh`
--

CREATE TABLE `xt_nganh` (
  `idnganh` int(11) NOT NULL,
  `manganh` varchar(45) NOT NULL,
  `tennganh` varchar(150) NOT NULL,
  `n_tohopgoc` varchar(10) DEFAULT NULL,
  `n_chitieu` int(11) NOT NULL DEFAULT 0,
  `n_diemsan` decimal(38,2) DEFAULT NULL,
  `n_diemtrungtuyen` decimal(38,2) DEFAULT NULL,
  `n_tuyenthang` varchar(1) DEFAULT NULL,
  `n_dgnl` varchar(1) DEFAULT NULL,
  `n_thpt` varchar(1) DEFAULT NULL,
  `n_vsat` varchar(1) DEFAULT NULL,
  `sl_xtt` int(11) DEFAULT NULL,
  `sl_dgnl` int(11) DEFAULT NULL,
  `sl_vsat` int(11) DEFAULT NULL,
  `sl_thpt` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `xt_nganh_tohop`
--

CREATE TABLE `xt_nganh_tohop` (
  `id` int(11) NOT NULL,
  `manganh` varchar(45) NOT NULL,
  `matohop` varchar(45) NOT NULL,
  `ten_nganh_chuan` varchar(255) DEFAULT NULL,
  `ten_to_hop` varchar(150) DEFAULT NULL,
  `la_tohop_goc` varchar(20) DEFAULT NULL,
  `th_mon1` varchar(10) DEFAULT NULL,
  `hsmon1` int(11) DEFAULT NULL,
  `th_mon2` varchar(10) DEFAULT NULL,
  `hsmon2` int(11) DEFAULT NULL,
  `th_mon3` varchar(10) DEFAULT NULL,
  `hsmon3` int(11) DEFAULT NULL,
  `tb_keys` varchar(80) DEFAULT NULL COMMENT 'manganh_matohop',
  `N1` int(11) DEFAULT NULL,
  `TO` int(11) DEFAULT NULL,
  `LI` int(11) DEFAULT NULL,
  `HO` int(11) DEFAULT NULL,
  `SI` int(11) DEFAULT NULL,
  `VA` int(11) DEFAULT NULL,
  `SU` int(11) DEFAULT NULL,
  `DI` int(11) DEFAULT NULL,
  `TI` int(11) DEFAULT NULL,
  `KHAC` int(11) DEFAULT NULL,
  `KTPL` int(11) DEFAULT NULL,
  `CNCN` tinyint(1) DEFAULT NULL,
  `CNNN` tinyint(1) DEFAULT NULL,
  `dolech` decimal(38,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `xt_nguoidung`
--

CREATE TABLE `xt_nguoidung` (
  `idnguoidung` int(11) NOT NULL,
  `taikhoan` varchar(45) NOT NULL,
  `matkhau` varchar(255) NOT NULL,
  `hoten` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `dienthoai` varchar(20) DEFAULT NULL,
  `phanquyen` varchar(45) NOT NULL DEFAULT 'user',
  `trangthaihoatdong` int(11) DEFAULT NULL,
  `ngaytao` datetime DEFAULT current_timestamp(),
  `ngaysua` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `xt_nguoidung`
--

INSERT INTO `xt_nguoidung` (`idnguoidung`, `taikhoan`, `matkhau`, `hoten`, `email`, `dienthoai`, `phanquyen`, `trangthaihoatdong`, `ngaytao`, `ngaysua`) VALUES
(1, 'admin', 'admin123', 'Administrator', 'admin@sgu.edu.vn', '0123456789', 'admin', 1, '2026-04-13 12:57:10', '2026-04-13 12:57:10'),
(2, 'user01', 'user123', 'User 01', 'user01@sgu.edu.vn', '0987654321', 'user', 1, '2026-04-13 12:57:10', '2026-04-13 12:57:10');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `xt_nguyenvongxettuyen`
--

CREATE TABLE `xt_nguyenvongxettuyen` (
  `idnv` int(11) NOT NULL,
  `nn_cccd` varchar(45) NOT NULL,
  `nv_tt` int(11) NOT NULL,
  `nv_manganh` varchar(45) NOT NULL,
  `nv_tenmanganh` varchar(255) DEFAULT NULL,
  `nv_tuyenthang` varchar(100) DEFAULT NULL,
  `diem_thxt` decimal(10,5) DEFAULT NULL COMMENT 'Da cong diem mon chinh',
  `diem_utqd` decimal(10,5) DEFAULT NULL COMMENT 'Diem UTQD theo to hop',
  `diem_cong` decimal(6,2) DEFAULT NULL,
  `diem_xettuyen` decimal(10,5) DEFAULT NULL COMMENT 'Da cong diem uu tien',
  `nv_ketqua` varchar(45) DEFAULT NULL,
  `nv_keys` varchar(45) DEFAULT NULL,
  `tt_phuongthuc` varchar(45) DEFAULT NULL,
  `tt_thm` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `xt_thisinhxettuyen25`
--

CREATE TABLE `xt_thisinhxettuyen25` (
  `idthisinh` int(11) NOT NULL,
  `cccd` varchar(20) DEFAULT NULL,
  `sobaodanh` varchar(45) DEFAULT NULL,
  `ho` varchar(100) DEFAULT NULL,
  `ten` varchar(100) DEFAULT NULL,
  `ngay_sinh` varchar(45) DEFAULT NULL,
  `dien_thoai` varchar(20) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `gioi_tinh` varchar(10) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `noi_sinh` varchar(100) DEFAULT NULL,
  `updated_at` varchar(255) DEFAULT NULL,
  `doi_tuong` varchar(45) DEFAULT NULL,
  `khu_vuc` varchar(45) DEFAULT NULL,
  `dan_toc` varchar(100) DEFAULT NULL,
  `ma_dan_toc` varchar(20) DEFAULT NULL,
  `chuong_trinh_hoc` varchar(50) DEFAULT NULL,
  `ma_mon_nn` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `xt_tohop_monthi`
--

CREATE TABLE `xt_tohop_monthi` (
  `idtohop` int(11) NOT NULL,
  `matohop` varchar(45) NOT NULL,
  `mon1` varchar(10) NOT NULL,
  `mon2` varchar(10) NOT NULL,
  `mon3` varchar(10) NOT NULL,
  `tentohop` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `xt_bangquydoi`
--
ALTER TABLE `xt_bangquydoi`
  ADD PRIMARY KEY (`idqd`),
  ADD UNIQUE KEY `d_maquydoi_UNIQUE` (`d_maquydoi`);

--
-- Chỉ mục cho bảng `xt_diemcongxetuyen`
--
ALTER TABLE `xt_diemcongxetuyen`
  ADD PRIMARY KEY (`iddiemcong`),
  ADD UNIQUE KEY `dc_keys_UNIQUE` (`dc_keys`);

--
-- Chỉ mục cho bảng `xt_diemthixettuyen`
--
ALTER TABLE `xt_diemthixettuyen`
  ADD PRIMARY KEY (`iddiemthi`),
  ADD UNIQUE KEY `cccd_phuongthuc_UNIQUE` (`cccd`,`d_phuongthuc`);

--
-- Chỉ mục cho bảng `xt_nganh`
--
ALTER TABLE `xt_nganh`
  ADD PRIMARY KEY (`idnganh`),
  ADD UNIQUE KEY `manganh_UNIQUE` (`manganh`);

--
-- Chỉ mục cho bảng `xt_nganh_tohop`
--
ALTER TABLE `xt_nganh_tohop`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `key_UNIQUE` (`tb_keys`);

--
-- Chỉ mục cho bảng `xt_nguoidung`
--
ALTER TABLE `xt_nguoidung`
  ADD PRIMARY KEY (`idnguoidung`),
  ADD UNIQUE KEY `taikhoan_UNIQUE` (`taikhoan`);

--
-- Chỉ mục cho bảng `xt_nguyenvongxettuyen`
--
ALTER TABLE `xt_nguyenvongxettuyen`
  ADD PRIMARY KEY (`idnv`),
  ADD UNIQUE KEY `nv_keys_UNIQUE` (`nv_keys`);

--
-- Chỉ mục cho bảng `xt_thisinhxettuyen25`
--
ALTER TABLE `xt_thisinhxettuyen25`
  ADD PRIMARY KEY (`idthisinh`),
  ADD UNIQUE KEY `cccd_UNIQUE` (`cccd`);

--
-- Chỉ mục cho bảng `xt_tohop_monthi`
--
ALTER TABLE `xt_tohop_monthi`
  ADD PRIMARY KEY (`idtohop`),
  ADD UNIQUE KEY `matohop_UNIQUE` (`matohop`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `xt_bangquydoi`
--
ALTER TABLE `xt_bangquydoi`
  MODIFY `idqd` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `xt_diemcongxetuyen`
--
ALTER TABLE `xt_diemcongxetuyen`
  MODIFY `iddiemcong` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `xt_diemthixettuyen`
--
ALTER TABLE `xt_diemthixettuyen`
  MODIFY `iddiemthi` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `xt_nganh`
--
ALTER TABLE `xt_nganh`
  MODIFY `idnganh` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `xt_nganh_tohop`
--
ALTER TABLE `xt_nganh_tohop`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT cho bảng `xt_nguoidung`
--
ALTER TABLE `xt_nguoidung`
  MODIFY `idnguoidung` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `xt_nguyenvongxettuyen`
--
ALTER TABLE `xt_nguyenvongxettuyen`
  MODIFY `idnv` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `xt_thisinhxettuyen25`
--
ALTER TABLE `xt_thisinhxettuyen25`
  MODIFY `idthisinh` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `xt_tohop_monthi`
--
ALTER TABLE `xt_tohop_monthi`
  MODIFY `idtohop` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
