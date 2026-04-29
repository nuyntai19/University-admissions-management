# Giải Đáp Thắc Mắc Dự Án Phần Mềm Tuyển Sinh

## Câu Hỏi 1: Lưu Trữ Điểm VSAT, DGNL, THPT Trong Database

### Vấn Đề
Hiện tại yêu cầu có 3 loại điểm: thi THPT, DGNL với VSAT. VSAT có nhiều loại điểm khác nhau (8 môn thi: Toán, Vật lý, Hóa học, Sinh học, Lịch sử, Địa lí, Tiếng Anh, Ngữ văn). Trong database v3 full import, không có cột riêng để nhập VSAT, vậy lưu các điểm này như thế nào?

### Giải Pháp
**Cách lưu trữ hiện tại trong dự án:**

#### 1. Bảng `xt_diemthixettuyen` (Lưu Điểm Thi)
```
Cấu trúc:
- cccd (PK): Mã số thí sinh (unique)
- d_phuongthuc: Loại phương thức (varchar) → "THPT" / "DGNL" / "V-SAT"
- TO, LI, HO, SI, SU, DI, VA: Điểm các môn (decimal)
- GDCD, N1_THI, N1_CC, CNCN, CNNN, TI, KTPL, NK1-NK10: Các cột khác cho môn đặc biệt
- diem_xet_tot_nghiep: Điểm tốt nghiệp (nếu dùng THPT)
```

**Quy Trình Nhập Và Lưu Điểm:**

**A. Đối với phương thức THPT:**
- Nhập 3 môn thi THPT (theo tổ hợp sinh học chọn)
- Ví dụ: Tổ hợp A00 (Toán, Vật lý, Hóa học) → nhập giá trị vào cột `TO`, `LI`, `HO`
- Điểm được lưu trực tiếp trên thang 10

**B. Đối với phương thức VSAT:**
- VSAT có 8 môn, mỗi thí sinh chọn 3 môn từ 8 môn có sẵn
- Nhập 3 môn VSAT → điểm được lưu vào các cột tương ứng (TO, LI, HO, SI, SU, DI, VA, GDCD...)
- Điểm VSAT ban đầu trên thang 150 → **phải quy đổi về thang 10** dựa vào **bảng quy đổi `xt_bangquydoi`**

**Bảng Quy Đổi (`xt_bangquydoi`):**
```
Cấu trúc:
- d_phuongthuc: Phương thức (VSAT, DGNL, THPT)
- d_tohop: Tổ hợp (A00, A01, B00, C00, ...)
- d_mon: Mã môn (TO, LI, HO, SI, SU, DI, VA, GDCD)
- d_diema - d_diemd: Khoảng điểm VSAT (ví dụ: 132 < x ≤ 150)
- Tương ứng với khoảng THPT (ví dụ: 8.5 < y ≤ 10)
- d_phanvi: Phân vị (3%, 5%, 10%, 20%, ..., >90%)
```

**Ví Dụ Quy Đổi VSAT Môn Toán:**
- Thí sinh có điểm VSAT Toán = 115 → khoảng (114.5, 122.5] → phân vị 20%
- Sử dụng công thức nội suy tuyến tính:
  ```
  y = c + ((x - a) / (b - a)) × (d - c)
  y = 7 + ((115 - 114.5) / (122.5 - 114.5)) × (7.75 - 7)
  y = 7 + (0.5 / 8) × 0.75 = 7.047
  ```
- Kết quả: Điểm VSAT 115 ≈ 7.047 điểm THPT

**C. Đối với phương thức ĐGNL:**
- ĐGNL là bài thi đánh giá năng lực (thang 1200 điểm hoặc thang tùy chỉnh)
- Nhập điểm ĐGNL → quy đổi về thang 30 dựa vào bảng `xt_bangquydoi`

---

### Quy Trình Lưu Trữ Chi Tiết

**Bước 1: Import Dữ Liệu Điểm**
- Nhập file Excel chứa điểm từ 3 phương thức
- Ứng dụng đọc file → phân loại phương thức → chuẩn bị dữ liệu

**Bước 2: Xử Lý & Chuẩn Hóa Điểm**
- Lưu loại phương thức vào cột `d_phuongthuc`
- Lưu điểm các môn vào các cột tương ứng (TO, LI, HO, SI, ...)

**Bước 3: Quy Đổi Điểm (Nếu Cần)**
```java
// Trong BUS Layer (DiemThiXetTuyenBUS.java hoặc BangQuyDoiBUS.java)
public BigDecimal convertVsatScore(String monThi, BigDecimal diemVsat) {
    // Tra bảng xt_bangquydoi
    // Tìm khoảng [a, b] chứa diemVsat
    // Tìm khoảng [c, d] tương ứng của THPT
    // Áp dụng công thức nội suy
    // return diemThpt
}
```

**Bước 4: Lưu Vào Database**
```sql
INSERT INTO xt_diemthixettuyen 
(cccd, d_phuongthuc, TO, LI, HO, ...)
VALUES
('TS_2385', 'V-SAT', 7.047, 8.5, 8.25, ...)
```

---

## Câu Hỏi 2: Quy Trình Xét Tuyển Chi Tiết

### Tổng Quan Quy Trình

**Xét tuyển = Quá trình tính điểm xét tuyển + Duyệt nguyện vọng + Xác định trúng tuyển**

---

### A. Tính Điểm Xét Tuyển

#### 1. Tính Tổng Điểm 3 Môn Thi (ĐTHXT)

**Công Thức Chung:**
```
ĐTHXT = [(d1 × w1 + d2 × w2 + d3 × w3) / W] × 3
```
Trong đó:
- `d1, d2, d3`: Điểm 3 môn thi (đã quy đổi về thang 10 nếu là VSAT/DGNL)
- `w1, w2, w3`: Hệ số từng môn (tra từ `xt_nganh_tohop`)
- `W = w1 + w2 + w3`

**Ví Dụ 1 (Phương Thức THPT):**
- Tổ hợp A00 (Toán, Vật lý, Hóa học): w1=1, w2=1, w3=1 → W=3
- Thí sinh có điểm: TO=8, LI=7, HO=7.5
- ĐTHXT = [(8×1 + 7×1 + 7.5×1) / 3] × 3 = (22.5/3) × 3 = 22.5

**Ví Dụ 2 (Phương Thức VSAT):**
- Tổ hợp A00 VSAT, mỗi môn trọng số khác nhau
- Điểm VSAT đã quy về thang 10 → áp dụng công thức tương tự

#### 2. Tính Điểm Ưu Tiên (ĐUT)

**Quy Tắc Tính ĐUT:**

Gọi ĐTHGXT = Điểm tổ hợp gốc (sau quy đổi từ tổ hợp thường sang tổ hợp gốc)

**Trường Hợp 1:** Nếu (ĐTHGXT + ĐC) < 22.5 điểm
```
ĐUT = MĐUT
(MĐUT: Mức điểm ưu tiên cố định theo đối tượng/khu vực)
```

**Trường Hợp 2:** Nếu (ĐTHGXT + ĐC) ≥ 22.5 điểm
```
ĐUT = [(30 - ĐTHGXT - ĐC) / 7.5] × MĐUT
(Công thức giảm dần theo điểm cao)
```

**Ví Dụ Tính ĐUT:**
- Thí sinh có ĐTHGXT = 20, ĐC = 1.5 → Tổng = 21.5 < 22.5
  → ĐUT = MĐUT (ví dụ MĐUT = 1.0 cho khu vực miền núi) → ĐUT = 1.0
  
- Thí sinh có ĐTHGXT = 24, ĐC = 1.5 → Tổng = 25.5 ≥ 22.5
  → ĐUT = [(30 - 24 - 1.5) / 7.5] × 1.0 = (4.5 / 7.5) × 1.0 = 0.6

#### 3. Tính Điểm Cộng (ĐC)

**Thành Phần Điểm Cộng:**
```
ĐC = Điểm cộng chứng chỉ ngoại ngữ + Điểm cộng đạt giải/ưu tiên
```

**Điểm Cộng Chứng Chỉ Ngoại Ngữ** (từ bảng `bangQuydoiTA_2025.txt`):
| Chứng chỉ | Mức 1 | Mức 2 | Mức 3 |
|-----------|-------|-------|--------|
| IELTS     | 4-5.0 | 5.5-6.5 | ≥7.0 |
| TOEFL ITP | 450-499 | 500-626 | ≥627 |
| VSTEP     | Bậc 3 | Bậc 4 | Bậc 5 |
| Mức điểm quy đổi (THPT thang 30) | 1.0 | 1.5 | 2.0 |
| Mức điểm quy đổi (VSAT thang 450) | 15 | 22.5 | 30 |

**Điểm Cộng Giải Thí Sinh Giỏi:**
- Quốc gia Giải Nhất: 2.0 / Giải Nhì: 1.5 / Giải Ba: 1.0 / Khuyến khích: 0.75
- Tỉnh Giải Nhất: 1.0 / Giải Nhì: 0.75 / Giải Ba: 0.5
- (Theo cột trong bảng thí sinh giỏi: `diem_cong_mon_giai` hoặc `diem_cong_khong_mon`)

**Điểm Ưu Tiên Xét Tuyển** (theo đối tượng/khu vực):
- Nạn nhân chất độc da cam: 1-2 điểm
- Hộ cận nghèo/khuyến tài: 0.5-1 điểm
- v.v... (từ bảng `Uu tien xet tuyen.txt`)

#### 4. Tính Tổng Điểm Xét Tuyển (ĐXT)

```
ĐXT = ĐTHGXT + ĐC + ĐUT
```

**Quy Tắc:** Tổng điểm xét tuyển **tối đa là 30 điểm**

---

### B. Duyệt Nguyện Vọng & Xác Định Trúng Tuyển

#### 1. Chuẩn Bị Dữ Liệu

**Tra Bảng `xt_nganh`:**
- Ngành 7140114 (Quản lý giáo dục):
  - Tổ hợp gốc: D01
  - Chỉ tiêu: 40 chỗ
  - Điểm chuẩn (ngưỡng đầu vào): 17.0 điểm

**Tra Bảng `xt_nganhohop`:**
- Ngành 7140114, Tổ hợp B03:
  - Đây là tổ hợp thường (không phải gốc)
  - Mức độ lệch so với tổ hợp gốc D01: -2.15 điểm

#### 2. Quy Trình Duyệt Nguyện Vọng (Cho Mỗi Thí Sinh)

**Bước 1:** Lấy danh sách nguyện vọng của thí sinh (từ `xt_nguyenvongxettuyen`), **sắp xếp theo `nv_tt` tăng dần**

```
Thí sinh TS_2385:
  NV1: Mã ngành 7140114 (Quản lý GD) 
  NV2: Mã ngành 7140202 (Giáo dục Tiểu học)
  NV3: Mã ngành 7140209 (SP Toán học)
  ...
```

**Bước 2:** Với mỗi nguyện vọng, **tính ĐXT (điểm xét tuyển)** cho nguyện vọng đó
- Tra tổ hợp môn của thí sinh → xác định phương thức (THPT/DGNL/VSAT)
- Lấy điểm 3 môn từ `xt_diemthixettuyen`
- Tính ĐTHXT (tổng điểm 3 môn)
- Quy đổi ĐTHXT về tổ hợp gốc (nếu khác tổ hợp gốc)
- Cộng ĐC (điểm cộng)
- Tính ĐUT (điểm ưu tiên)
- Cộng lại: **ĐXT = ĐTHGXT + ĐC + ĐUT**

**Bước 3:** Kiểm tra điều kiện trúng tuyển

```
if (ĐXT ≥ n_diemsan của ngành) AND (số chỉ tiêu còn lại > 0) {
    // Trúng tuyển
    nv_ketqua = "ĐỖ"
    Cập nhật: số chỉ tiêu--
    DỪNG duyệt các NV tiếp theo
} else if (ĐXT < n_diemsan) {
    // Trượt
    nv_ketqua = "TRƯỢT"
    Tiếp tục NV kế tiếp
} else if (số chỉ tiêu = 0) {
    // Ngành hết chỉ tiêu
    nv_ketqua = "TRƯỢT" hoặc "CHƯA NHẬN"
    Tiếp tục NV kế tiếp
}
```

**Bước 4:** Nếu trúng NV nào → **tất cả NV sau đó = "KHÔNG XÉT"** (hủy)

---

### C. Ví Dụ Xét Tuyển Cụ Thể

**Thí Sinh TS_2385 (CCCD: 0123456789):**

**Bước 1: Lấy Thông Tin Thí Sinh**
```
Phương thức: THPT
Tổ hợp: A00 (Toán, Vật lý, Hóa học)
Điểm thi: TO=8.5, LI=8.0, HO=7.5
Đối tượng: Quốc gia HSG môn Toán, Giải Nhì → ĐC chứng chỉ = 2.0, ĐC giải = 1.5
Khu vực: Miền núi → MĐUT = 0.75
```

**Bước 2: Duyệt NV1 (Quản lý Giáo dục - 7140114)**

1. Tính ĐTHXT:
   ```
   ĐTHXT = [(8.5×1 + 8.0×1 + 7.5×1) / 3] × 3 = (24/3) × 3 = 24
   ```

2. Quy đổi về tổ hợp gốc D01 (tra bảng quy đổi):
   ```
   ĐTHGXT_A00 → ĐTHGXT_D01 = 24 - (-0.69) = 24.69
   ```
   (Áp dụng bảng độ lệch từ `cac cong thuc tinh.txt`)

3. Tính ĐC:
   ```
   ĐC = 2.0 (chứng chỉ) + 1.5 (giải) = 3.5 (nhưng tối đa 3.0) = 3.0
   ```

4. Tính ĐUT:
   ```
   ĐTHGXT + ĐC = 24.69 + 3.0 = 27.69 ≥ 22.5
   ĐUT = [(30 - 24.69 - 3.0) / 7.5] × 0.75 
       = (2.31 / 7.5) × 0.75 = 0.231
   ```

5. Tính ĐXT:
   ```
   ĐXT = 24.69 + 3.0 + 0.231 = 27.921 ≈ 27.92
   ```

6. Kiểm tra điều kiện:
   ```
   ĐXT (27.92) ≥ n_diemsan (17.0)? YES ✓
   Chỉ tiêu còn lại > 0? YES (40 > 0) ✓
   → KẾT QUẢ: ĐỖ NV1
   ```

**Bước 3: Kết Luận**
- **NV1: ĐỖ** (Quản lý Giáo dục)
- **NV2, NV3, ...: KHÔNG XÉT** (tự động hủy vì đã trúng NV1)

---

### D. Cách Triển Khai Trong Code

#### 1. BUS Layer - Tính Toán Điểm

```java
// File: DiemCongXetTuyenBUS.java (hoặc NguyenVongXetTuyenBUS.java)

public BigDecimal tinhDiemXetTuyen(
    String cccd,           // Mã thí sinh
    String maNganh,        // Mã ngành xét
    String maTohop        // Mã tổ hợp
) {
    // 1. Lấy điểm 3 môn từ diemDAO
    DiemThiXetTuyen diem = diemDAO.getByCccd(cccd);
    
    // 2. Lấy hệ số môn từ nganhToHopDAO
    NganhToHop ngt = nganhToHopDAO.getByKey(maNganh, maTohop);
    
    // 3. Tính ĐTHXT
    BigDecimal diemMon1 = getMonThi(diem, ngt.getTh_mon1());
    BigDecimal diemMon2 = getMonThi(diem, ngt.getTh_mon2());
    BigDecimal diemMon3 = getMonThi(diem, ngt.getTh_mon3());
    
    BigDecimal heSo = new BigDecimal(ngt.getHsmon1() + ngt.getHsmon2() + ngt.getHsmon3());
    BigDecimal diemTongMon = 
        diemMon1.multiply(new BigDecimal(ngt.getHsmon1()))
        .add(diemMon2.multiply(new BigDecimal(ngt.getHsmon2())))
        .add(diemMon3.multiply(new BigDecimal(ngt.getHsmon3())))
        .divide(heSo, 3, RoundingMode.HALF_UP)
        .multiply(new BigDecimal(3));
    
    // 4. Quy đổi từ tổ hợp thường về tổ hợp gốc
    BigDecimal diemTohopGoc = quyDoiTohop(diemTongMon, ngt);
    
    // 5. Lấy ĐC (điểm cộng)
    DiemCongXetTuyen diemCong = diemCongDAO.getByCccdAndNganh(cccd, maNganh);
    BigDecimal dc = diemCong != null ? diemCong.getDiemTong() : BigDecimal.ZERO;
    
    // 6. Tính ĐUT
    BigDecimal diemUT = tinhDiemUT(diemTohopGoc, dc, thiSinh);
    
    // 7. Tính tổng ĐXT
    BigDecimal diemXT = diemTohopGoc.add(dc).add(diemUT);
    
    return diemXT.min(new BigDecimal(30)); // Tối đa 30 điểm
}
```

#### 2. BUS Layer - Xét Tuyển Nguyện Vọng

```java
// File: NguyenVongXetTuyenBUS.java

public void xetTuyenToAnNguyenvong(String cccd) {
    // 1. Lấy danh sách NV của thí sinh, sort by nv_tt
    List<NguyenVongXetTuyen> danhSachNV = 
        nvDAO.getByCccd(cccd); // phải sort trong query
    
    // 2. Khởi tạo bản đồ chỉ tiêu còn lại của từng ngành
    Map<String, Integer> chiTieuConLai = loadChiTieuConLai();
    
    // 3. Duyệt từng NV
    for (NguyenVongXetTuyen nv : danhSachNV) {
        // 3.1. Kiểm tra nv đã trúng hay chưa
        if (daTrungNguyen) break; // Nếu trúng rồi thì dừng
        
        // 3.2. Tính ĐXT cho NV này
        BigDecimal diemXT = tinhDiemXetTuyen(
            cccd, 
            nv.getNv_manganh(),
            layMaTohopThiSinh(cccd, nv.getNv_manganh())
        );
        
        nv.setDiem_xettuyen(diemXT);
        
        // 3.3. Lấy điểm chuẩn của ngành
        Nganh nganh = nganhDAO.getByMaNganh(nv.getNv_manganh());
        BigDecimal diemCuan = nganh.getN_diemsan();
        
        // 3.4. Kiểm tra điều kiện trúng tuyển
        if (diemXT.compareTo(diemCuan) >= 0 && 
            chiTieuConLai.get(nv.getNv_manganh()) > 0) {
            // TRÚNG
            nv.setNv_ketqua("ĐỖ");
            chiTieuConLai.put(nv.getNv_manganh(), 
                chiTieuConLai.get(nv.getNv_manganh()) - 1);
            daTrungNguyen = true;
        } else {
            // TRƯỢT
            nv.setNv_ketqua("TRƯỢT");
        }
        
        // 3.5. Lưu kết quả NV vào DB
        nvDAO.update(nv);
    }
    
    // 4. Tất cả NV còn lại → "KHÔNG XÉT"
    for (int i = 0; i < danhSachNV.size(); i++) {
        if (danhSachNV.get(i).getNv_ketqua() == null) {
            danhSachNV.get(i).setNv_ketqua("KHÔNG XÉT");
            nvDAO.update(danhSachNV.get(i));
        }
    }
}
```

---

### E. Các Giao Diện Liên Quan

#### 1. **Giao Diện Điểm Thi** (`DiemThiXetTuyenGUI.java`)
   - Hiển thị form nhập/sửa điểm 3 môn thi
   - Chọn phương thức (THPT / DGNL / VSAT)
   - Import dữ liệu điểm từ Excel
   - Xem danh sách, tìm kiếm theo CCCD

#### 2. **Giao Diện Điểm Cộng** (`DiemCongXetTuyenGUI.java`)
   - Nhập/sửa ĐC chứng chỉ ngoại ngữ
   - Nhập/sửa ĐC giải thí sinh giỏi
   - Nhập/sửa ĐC ưu tiên (đối tượng/khu vực)
   - Hiển thị tổng ĐC

#### 3. **Giao Diện Nguyện Vọng** (`NguyenVongXetTuyenGUI.java`)
   - Hiển thị danh sách NV của thí sinh
   - Nút "Xét Tuyển" → gọi hàm xetTuyenToAnNguyenvong()
   - Hiển thị kết quả: ĐỖ / TRƯỢT / KHÔNG XÉT
   - Hiển thị chi tiết điểm ĐXT từng NV
   - Xuất báo cáo xét tuyển toàn trường

#### 4. **Giao Diện Bảng Quy Đổi** (`BangQuyDoiGUI.java`)
   - Quản lý (CRUD) dữ liệu bảng quy đổi
   - Lọc theo phương thức / tổ hợp / môn thi
   - Import bảng quy đổi từ file Excel

---

## Tóm Tắt

### 1. Lưu Trữ Điểm
- **Bảng `xt_diemthixettuyen`**: Lưu điểm 3 môn từ 3 phương thức (THPT/DGNL/VSAT)
- **Bảng `xt_bangquydoi`**: Lưu bảng quy đổi điểm từ VSAT/DGNL về thang 10
- **Cơ chế**: Nhập điểm gốc → quy đổi nếu cần → lưu vào DB

### 2. Xét Tuyển
- **Tính Điểm**: ĐTHXT → Quy đổi tổ hợp → Cộng ĐC → Cộng ĐUT → ĐXT
- **Duyệt NV**: Từ NV1 đến NVn, kiểm tra điều kiện (điểm ≥ chuẩn + chỉ tiêu > 0)
- **Kết Quả**: ĐỖ (NV nào) / TRƯỢT (NV nào) / KHÔNG XÉT (NV còn lại)
- **Nguyên Tắc**: Chỉ được trúng 1 NV duy nhất, các NV sau tự động hủy
