-- E2E seed data for: Ngành - Tổ Hợp (auto môn + độ lệch) + Bảng Quy Đổi
-- Target DB: xettuyen2026 (Docker compose MySQL)

USE xettuyen2026;

-- 1) Seed subject-combination catalog
INSERT INTO xt_tohop_monthi (matohop, mon1, mon2, mon3, tentohop)
VALUES
  ('A00', 'TO', 'LI', 'HO', 'Toán - Lý - Hóa'),
  ('A01', 'TO', 'LI', 'VA', 'Toán - Lý - Văn'),
  ('D01', 'TO', 'VA', 'AN', 'Toán - Văn - Anh')
ON DUPLICATE KEY UPDATE
  mon1 = VALUES(mon1),
  mon2 = VALUES(mon2),
  mon3 = VALUES(mon3),
  tentohop = VALUES(tentohop);

-- 2) Seed 1 major (ngành) with tổ hợp gốc = A00 (important for độ lệch)
-- NOTE: xt_nganh currently has no UNIQUE constraint on manganh, so we delete first to avoid duplicates.
DELETE FROM xt_nganh WHERE manganh = '7777777';
INSERT INTO xt_nganh (manganh, tennganh, n_tohopgoc, n_chitieu, n_diemsan, n_diemtrungtuyen, n_tuyenthang, n_dgnl, n_thpt, n_vsat,
                     sl_xtt, sl_dgnl, sl_vsat, sl_thpt)
VALUES
  ('7777777', 'TEST - Công nghệ thông tin', 'A00', 10, NULL, NULL, '0', '1', '1', '1',
   0, 0, 0, NULL);

-- 3) Clear mapping row if it existed before (you will add via UI to verify auto-fill + duplicate guard)
DELETE FROM xt_nganh_tohop WHERE tb_keys = '7777777_A01';

-- 4) Seed score-conversion rows (Bảng Quy Đổi)
-- These rows let you test:
-- - derived key (d_maquydoi)
-- - duplicate constraint (unique d_maquydoi)
-- - interpolation via JShell using BangQuyDoiBUS.quyDoiNoiSuy(...)
INSERT INTO xt_bangquydoi (d_phuongthuc, d_tohop, d_mon, d_phanvi, d_diema, d_diemb, d_diemc, d_diemd, d_maquydoi)
VALUES
  ('V-SAT', NULL, 'TO', 'P50', 5.00, 6.00, 15.00, 18.00, 'V-SAT_TO_P50'),
  ('V-SAT', NULL, 'TO', 'P60', 6.00, 7.00, 18.00, 21.00, 'V-SAT_TO_P60'),
  ('DGNL',  'A01', NULL, 'P50', 600.00, 700.00, 15.00, 18.00, 'DGNL_A01_P50')
ON DUPLICATE KEY UPDATE
  d_phuongthuc = VALUES(d_phuongthuc),
  d_tohop = VALUES(d_tohop),
  d_mon = VALUES(d_mon),
  d_phanvi = VALUES(d_phanvi),
  d_diema = VALUES(d_diema),
  d_diemb = VALUES(d_diemb),
  d_diemc = VALUES(d_diemc),
  d_diemd = VALUES(d_diemd);
