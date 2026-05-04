-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: xettuyen2026
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `xt_bangquydoi`
--

DROP TABLE IF EXISTS `xt_bangquydoi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_bangquydoi` (
  `idqd` int NOT NULL AUTO_INCREMENT,
  `d_phuongthuc` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `d_tohop` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `d_mon` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `d_diema` decimal(38,2) DEFAULT NULL,
  `d_diemb` decimal(38,2) DEFAULT NULL,
  `d_diemc` decimal(38,2) DEFAULT NULL,
  `d_diemd` decimal(38,2) DEFAULT NULL,
  `d_maquydoi` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `d_phanvi` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idqd`),
  UNIQUE KEY `d_maquydoi_UNIQUE` (`d_maquydoi`)
) ENGINE=InnoDB AUTO_INCREMENT=97 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xt_diemcongxetuyen`
--

DROP TABLE IF EXISTS `xt_diemcongxetuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_diemcongxetuyen` (
  `iddiemcong` int unsigned NOT NULL AUTO_INCREMENT,
  `ts_cccd` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `manganh` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `matohop` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phuongthuc` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `diemCC` decimal(38,2) DEFAULT NULL,
  `diemUtxt` decimal(38,2) DEFAULT NULL,
  `diemTong` decimal(38,2) DEFAULT NULL,
  `ghichu` text COLLATE utf8mb4_unicode_ci,
  `dc_keys` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cap_giai` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `doi_tuong_giai` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ma_mon_giai` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `loai_giai` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `diem_cong_mon_giai` decimal(38,2) DEFAULT NULL,
  `diem_cong_khong_mon` decimal(38,2) DEFAULT NULL,
  `co_chung_chi` tinyint(1) DEFAULT NULL,
  `chung_chi_ngoai_ngu` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `diem_chung_chi` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `diem_quy_doi_chung_chi` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`iddiemcong`),
  UNIQUE KEY `dc_keys_UNIQUE` (`dc_keys`)
) ENGINE=InnoDB AUTO_INCREMENT=9327 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xt_diemthixettuyen`
--

DROP TABLE IF EXISTS `xt_diemthixettuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_diemthixettuyen` (
  `iddiemthi` int NOT NULL AUTO_INCREMENT,
  `cccd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sobaodanh` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `d_phuongthuc` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
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
  `diem_xet_tot_nghiep` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`iddiemthi`),
  UNIQUE KEY `cccd_phuongthuc_UNIQUE` (`cccd`,`d_phuongthuc`),
  UNIQUE KEY `UKfhm3o5xso39x2e1ts1ogmn89b` (`cccd`,`d_phuongthuc`)
) ENGINE=InnoDB AUTO_INCREMENT=49382 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xt_nganh`
--

DROP TABLE IF EXISTS `xt_nganh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_nganh` (
  `idnganh` int NOT NULL AUTO_INCREMENT,
  `manganh` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tennganh` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `n_tohopgoc` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `n_chitieu` int NOT NULL DEFAULT '0',
  `n_diemsan` decimal(38,2) DEFAULT NULL,
  `n_diemtrungtuyen` decimal(38,2) DEFAULT NULL,
  `n_tuyenthang` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `n_dgnl` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `n_thpt` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `n_vsat` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sl_xtt` int DEFAULT NULL,
  `sl_dgnl` int DEFAULT NULL,
  `sl_vsat` int DEFAULT NULL,
  `sl_thpt` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idnganh`),
  UNIQUE KEY `manganh_UNIQUE` (`manganh`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xt_nganh_tohop`
--

DROP TABLE IF EXISTS `xt_nganh_tohop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_nganh_tohop` (
  `id` int NOT NULL AUTO_INCREMENT,
  `manganh` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `matohop` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ten_nganh_chuan` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ten_to_hop` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `la_tohop_goc` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `th_mon1` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hsmon1` int DEFAULT NULL,
  `th_mon2` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hsmon2` int DEFAULT NULL,
  `th_mon3` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hsmon3` int DEFAULT NULL,
  `tb_keys` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'manganh_matohop',
  `N1` int DEFAULT NULL,
  `TO` int DEFAULT NULL,
  `LI` int DEFAULT NULL,
  `HO` int DEFAULT NULL,
  `SI` int DEFAULT NULL,
  `VA` int DEFAULT NULL,
  `SU` int DEFAULT NULL,
  `DI` int DEFAULT NULL,
  `TI` int DEFAULT NULL,
  `KHAC` int DEFAULT NULL,
  `KTPL` int DEFAULT NULL,
  `CNCN` int DEFAULT NULL,
  `CNNN` int DEFAULT NULL,
  `dolech` decimal(38,2) DEFAULT NULL,
  `GDCD` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_UNIQUE` (`tb_keys`)
) ENGINE=InnoDB AUTO_INCREMENT=1465 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xt_nguoidung`
--

DROP TABLE IF EXISTS `xt_nguoidung`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_nguoidung` (
  `idnguoidung` int NOT NULL AUTO_INCREMENT,
  `taikhoan` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `matkhau` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `hoten` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dienthoai` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phanquyen` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user',
  `trangthaihoatdong` int DEFAULT NULL,
  `ngaytao` datetime DEFAULT CURRENT_TIMESTAMP,
  `ngaysua` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idnguoidung`),
  UNIQUE KEY `taikhoan_UNIQUE` (`taikhoan`)
) ENGINE=InnoDB AUTO_INCREMENT=49384 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xt_nguyenvongxettuyen`
--

DROP TABLE IF EXISTS `xt_nguyenvongxettuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xt_thisinhxettuyen25`
--

DROP TABLE IF EXISTS `xt_thisinhxettuyen25`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_thisinhxettuyen25` (
  `idthisinh` int NOT NULL AUTO_INCREMENT,
  `cccd` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sobaodanh` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ho` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ten` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ngay_sinh` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dien_thoai` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gioi_tinh` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `noi_sinh` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `doi_tuong` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `khu_vuc` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dan_toc` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ma_dan_toc` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `chuong_trinh_hoc` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ma_mon_nn` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idthisinh`),
  UNIQUE KEY `cccd_UNIQUE` (`cccd`)
) ENGINE=InnoDB AUTO_INCREMENT=49382 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xt_tohop_monthi`
--

DROP TABLE IF EXISTS `xt_tohop_monthi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_tohop_monthi` (
  `idtohop` int NOT NULL AUTO_INCREMENT,
  `matohop` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `mon1` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `mon2` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `mon3` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tentohop` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idtohop`),
  UNIQUE KEY `matohop_UNIQUE` (`matohop`)
) ENGINE=InnoDB AUTO_INCREMENT=219 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-04 10:27:12
