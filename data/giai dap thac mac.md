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

### Kết Luận Riêng Về Database V3 Full Import

**Trả lời ngắn gọn:**
- **Không**, database v3 full import **không phải là thiếu hoàn toàn** cột để lưu điểm DGNL và V-SAT.
- **Có**, bảng `xt_diemthixettuyen` đã có sẵn cột `d_phuongthuc` để phân loại phương thức và có các cột điểm như `NL1`, `TO`, `LI`, `HO`, `SI`, `SU`, `DI`, `VA`, `GDCD`, `KTPL`, `NK1-NK10` để lưu dữ liệu điểm.
- **Nhưng** database hiện tại **không tách riêng** chỗ lưu điểm gốc của DGNL/V-SAT với điểm sau quy đổi. Nếu bạn muốn giữ cả **điểm thô** và **điểm đã quy đổi**, thì cần sửa thêm schema.

**Cách hiểu đúng với project hiện tại:**
- **THPT**: lưu trực tiếp điểm môn thi trên thang 10.
- **DGNL**: lưu điểm bài thi vào `NL1` nếu dùng như điểm gốc, hoặc quy đổi về thang điểm xét tuyển trước khi lưu nếu bạn muốn đồng nhất dữ liệu.
- **V-SAT**: vì dữ liệu V-SAT gốc không nằm trên thang 10, nên thường phải **quy đổi trước** rồi mới lưu vào các cột môn tương ứng.

**Khi nào chưa cần sửa database:**
- Nếu quy ước của hệ thống là: **điểm đầu vào được quy đổi xong rồi mới lưu**.
- Nếu chỉ cần lưu 1 giá trị dùng để xét tuyển, không cần giữ lại điểm gốc ban đầu.

**Khi nào nên sửa database:**
- Nếu bạn muốn import và lưu đồng thời **điểm gốc** lẫn **điểm quy đổi**.
- Nếu bạn muốn truy vết rõ thí sinh thuộc phương thức nào và điểm thô ban đầu là bao nhiêu.
- Nếu bạn muốn import dữ liệu V-SAT/DGNL từ file nguồn mà chưa quy đổi, rồi hệ thống tự quy đổi sau.

**Gợi ý sửa nếu muốn đúng và rõ hơn với yêu cầu project:**
1. Giữ nguyên `d_phuongthuc`.
2. Thêm cột lưu điểm gốc riêng, ví dụ `diem_goc`, `thang_diem_goc`, hoặc thêm bộ cột riêng cho DGNL/V-SAT.
3. Hoặc tạo bảng riêng cho điểm nguồn, rồi bảng `xt_diemthixettuyen` chỉ lưu dữ liệu đã chuẩn hóa để xét tuyển.

**Khuyến nghị thực tế cho đồ án:**
- Nếu bạn muốn hệ thống đơn giản, **không cần sửa DB quá nhiều**; chỉ cần sửa file import để luôn có `d_phuongthuc` và quy đổi V-SAT/DGNL trước khi lưu.
- Nếu giảng viên yêu cầu quản lý đầy đủ dữ liệu gốc, thì **nên thêm cột/bảng mới** để tách “điểm thô” và “điểm đã quy đổi”.

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

---

## Câu Hỏi 3: Giao Diện Quy Đổi Điểm Đang Hoạt Động Như Thế Nào?

### Kết Luận Ngắn Gọn
- Trong source code hiện tại, màn hình **Bảng Quy Đổi** không phải là màn hình tự tính điểm ngay lập tức cho thí sinh.
- Đây là màn hình **quản lý bảng tra quy đổi**: nhập dữ liệu mốc `a, b, c, d`, phương thức, tổ hợp, môn, phân vị và mã quy đổi.
- Hàm quy đổi thực sự nằm ở tầng BUS, dùng tra khoảng điểm trong bảng rồi áp dụng **nội suy tuyến tính**.
- Các màn hình khác trong app hiện **chưa gọi trực tiếp** hàm quy đổi này; chúng chỉ được gắn chung vào cùng ứng dụng và cùng cơ sở dữ liệu.

### 1. Màn Hình Này Làm Gì Trong Giao Diện?

File giao diện là `BangQuyDoiGUI.java`.

Màn hình có các thành phần chính:
- Chọn **phương thức**: `DGNL`, `V-SAT`, `THPT`
- Nhập **tổ hợp** nếu có
- Nhập **môn** nếu có
- Nhập **phân vị**
- Nhập 4 mốc quy đổi `a`, `b`, `c`, `d`
- Hiển thị **mã quy đổi** tự sinh
- Bảng danh sách phía dưới để xem toàn bộ cấu hình quy đổi đã lưu

Thao tác trên màn hình:
- **Thêm**: tạo bản ghi mới
- **Sửa**: cập nhật bản ghi đang chọn
- **Xóa**: xóa bản ghi
- **Làm Mới**: xóa dữ liệu form

Điểm quan trọng: màn hình này chỉ quản lý dữ liệu quy đổi, không có nút tính điểm tự động cho thí sinh ngay tại UI.

### 2. Cách Quy Đổi Điểm Trong Code

Luồng xử lý nằm ở `BangQuyDoiBUS.java` và `BangQuyDoiDAO.java`.

#### Bước 1: Tìm đúng khoảng quy đổi
Khi gọi `quyDoiNoiSuy(phuongThuc, toHop, mon, x)`, BUS sẽ:
- Chuẩn hóa dữ liệu về chữ in hoa và bỏ khoảng trắng
- Tra bảng `xt_bangquydoi`
- Tìm bản ghi có:
    - đúng phương thức
    - đúng tổ hợp nếu có
    - đúng môn nếu có
    - và điểm `x` nằm trong khoảng `a < x <= b`

DAO có hai kiểu tìm:
- `findIntervalExclusiveLower(...)`: ưu tiên điều kiện `x > a` và `x <= b`
- `findIntervalInclusive(...)`: phương án dự phòng với `x >= a` và `x <= b`

#### Bước 2: Áp dụng nội suy tuyến tính
Sau khi tìm được mốc, hệ thống tính:

`y = c + ((x - a) / (b - a)) * (d - c)`

Trong đó:
- `x` là điểm gốc đầu vào
- `[a, b]` là khoảng điểm gốc trong bảng quy đổi
- `[c, d]` là khoảng điểm quy đổi tương ứng
- `y` là điểm sau quy đổi

Nếu `b = a` thì BUS trả luôn `d` để tránh chia cho 0.

#### Bước 3: Kết quả trả về
Hàm trả về `BigDecimal` và làm tròn đến 5 chữ số thập phân.

### 3. Dữ Liệu Quy Đổi Được Lưu Ở Đâu?

Bảng dữ liệu là `xt_bangquydoi` trong database.

Các cột chính:
- `d_phuongthuc`: phương thức quy đổi
- `d_tohop`: tổ hợp, có thể rỗng
- `d_mon`: môn, có thể rỗng
- `d_phanvi`: phân vị
- `d_diema`, `d_diemb`: mốc điểm gốc
- `d_diemc`, `d_diemd`: mốc điểm quy đổi
- `d_maquydoi`: khóa suy ra tự động từ các trường trên

Trong `BangQuyDoiBUS`, `d_maquydoi` được tạo theo mẫu:
- `PHUONGTHUC[_TOHOP][_MON]_PHANVI`

Ví dụ:
- `V-SAT_TO_20%`
- `DGNL_A01_10%`

Hệ thống cũng chặn trùng khóa này trước khi lưu.

### 4. Dữ Liệu Trong Folder `data` Liên Quan Thế Nào?

Các file trong `data` không phải mã chạy trực tiếp, nhưng là nguồn nội dung để hiểu và nạp bảng quy đổi:
- `Quy doi diem thi V-SAT 2025.txt`: mô tả quy đổi V-SAT sang THPT theo bách phân vị
- `cac cong thuc tinh.txt`: mô tả công thức quy đổi tổ hợp, điểm cộng, điểm ưu tiên và điểm xét tuyển
- `bangQUyDoiTA_2025.txt`: bảng quy đổi chứng chỉ ngoại ngữ sang điểm môn Tiếng Anh và điểm cộng

Nói ngắn gọn, folder `data` cung cấp quy tắc nghiệp vụ, còn `xt_bangquydoi` là nơi lưu dữ liệu quy đổi để hệ thống tra cứu.

### 5. Màn Hình Này Liên Kết Với Các Giao Diện Khác Ra Sao?

Trong source hiện tại, liên kết là theo 2 lớp:

#### Liên kết ở mức điều hướng
Màn hình `BangQuyDoiGUI` được gắn vào:
- thanh sidebar của `PhanMemTuyenSinh.java`
- tab của `PhanMemTuyenSinhApp.java`

Tức là người dùng mở app sẽ thấy riêng một mục “Bảng quy đổi” để vào quản lý bảng tra.

#### Liên kết ở mức dữ liệu
Các màn hình khác dùng chung dữ liệu nghiệp vụ nhưng không gọi trực tiếp hàm quy đổi này trong code hiện tại:
- `DiemThiXetTuyenGUI.java`: quản lý dữ liệu điểm thi gốc, import và lọc danh sách
- `DiemCongXetTuyenGUI.java`: quản lý điểm cộng, hiển thị phần “quy đổi tự động” cho điểm cộng
- `NguyenVongXetTuyenGUI.java`: hiển thị điểm xét tuyển, điểm cộng, điểm ưu tiên và kết quả xét tuyển

Vì vậy, bảng quy đổi đóng vai trò **bảng tra trung gian**. Nó là dữ liệu nền để phục vụ tính toán, chứ không phải một workflow độc lập trong giao diện như nhập điểm hay nhập nguyện vọng.

### 6. Cách Hiểu Đúng Với Project Hiện Tại

- Nếu chỉ nhìn giao diện, màn hình quy đổi là nơi CRUD bảng cấu hình.
- Nếu nhìn tầng BUS, quy đổi là phép nội suy từ `a-b` sang `c-d`.
- Nếu nhìn toàn hệ thống, bảng này là dữ liệu nền để hỗ trợ quy đổi điểm giữa các phương thức và phục vụ các bước xét tuyển sau đó.
- Hiện tại, source chưa cho thấy một màn hình nào khác tự động gọi `quyDoiNoiSuy(...)` khi người dùng bấm nút trên giao diện.

### 7. Tóm Tắt Một Dòng

`BangQuyDoiGUI` = nơi nhập và quản lý bảng tra; `BangQuyDoiBUS.quyDoiNoiSuy(...)` = nơi tính quy đổi; các màn hình điểm thi, điểm cộng, nguyện vọng = nơi dùng dữ liệu đầu vào và hiển thị kết quả xét tuyển.

---

## Câu Hỏi 4: Bảng `xt_diemthixettuyen` - Cột NL1, Các Phương Thức & d_phuongthuc

### Vấn Đề
Bảng `xt_diemthixettuyen` dùng để lưu 3 loại điểm THPT, VSAT, DGNL với cột `d_phuongthuc` để phân loại. Nhưng với phương thức VSAT và DGNL, đã có cột để lưu 2 loại phương thức này chưa? Có phải `NL1` dùng để lưu điểm ĐGNL không?

### Trả Lời Chi Tiết

#### 1. **Cấu Trúc Cột Hiện Tại**

Bảng `xt_diemthixettuyen` có sẵn các cột sau (theo Entity Java):

| Cột | Kiểu Dữ Liệu | Mục Đích |
|-----|--------------|---------|
| `d_phuongthuc` | VARCHAR(10) | **Phân loại phương thức**: "THPT", "V-SAT", "DGNL" |
| `TO, LI, HO, SI, SU, DI, VA` | DECIMAL | 7 môn học cơ bản (dùng chung cho THPT & VSAT) |
| `GDCD` | DECIMAL | Giáo dục công dân (năng khiếu) |
| `NL1` | DECIMAL | **Năng Lực 1 = Điểm ĐGNL gốc** |
| `N1_THI, N1_CC` | DECIMAL | Điểm ngoại ngữ (tiếng Anh): từ thi và từ chứng chỉ |
| `CNCN, CNNN, TI, KTPL` | DECIMAL | Các môn đặc biệt khác |
| `NK1 - NK10` | DECIMAL | Năng khiếu 1-10 |
| `diem_xet_tot_nghiep` | DECIMAL | Điểm tốt nghiệp (THPT) |

#### 2. **`NL1` Là Gì? Có Phải Lưu Điểm ĐGNL?**

**Đúng!** `NL1` = **Năng Lực 1**, được sử dụng để lưu **điểm ĐGNL gốc** (trước khi quy đổi).

**Quy ước:**
- Khi `d_phuongthuc = "DGNL"`, giá trị `NL1` chứa **điểm bài thi ĐGNL ban đầu** (thang cao, ví dụ 0-1200)
- Khi `d_phuongthuc = "THPT"` hoặc `"V-SAT"`, cột `NL1` có thể để NULL hoặc 0
- Khi cần lấy điểm xét tuyển, hệ thống tra bảng `xt_bangquydoi` để quy đổi `NL1` từ thang cao về thang 30

**Ví dụ:**
```
Thí sinh TS_001 chọn phương thức DGNL:
  cccd: 0123456789
  d_phuongthuc: "DGNL"
  NL1: 850.0  (Điểm ĐGNL gốc, thang 1200)
  TO, LI, HO, ...: NULL (không dùng)
  
Sau đó, tra bảng xt_bangquydoi với:
  d_phuongthuc = "DGNL"
  d_tohop = "A01" (hoặc tổ hợp của ngành xét)
  Tìm khoảng chứa 850 → quy đổi sang [c, d] → ra ~22.5 thang 30
```

#### 3. **Có Phương Thức VSAT & DGNL Không?**

**Không cần thêm cột!** Hệ thống đã thiết kế rất hợp lý:

- **Cột `d_phuongthuc`** làm nhiệm vụ phân loại duy nhất (THPT / VSAT / DGNL)
- **Các cột điểm hiện có** đủ để lưu dữ liệu:
  - **THPT**: dùng `TO, LI, HO, SI, SU, DI, VA` (7 môn chính)
  - **VSAT**: dùng `TO, LI, HO, SI, SU, DI, VA, GDCD` (8 môn VSAT)
  - **DGNL**: dùng cột `NL1` để lưu điểm gốc

#### 4. **Mapping Chi Tiết Giữa Phương Thức & Cột**

```
┌─────────────────────────────────────────────────────┐
│          PHƯƠNG THỨC           │      CỘT DÙNG      │
├─────────────────────────────────────────────────────┤
│ d_phuongthuc = "THPT"          │ TO, LI, HO         │
│ d_phuongthuc = "V-SAT"         │ TO, LI, HO, SI,... │
│ d_phuongthuc = "DGNL"          │ NL1                │
│ Ngoài ngữ (nếu có)             │ N1_THI, N1_CC      │
│ Năng khiếu (nếu có)            │ NK1-NK10           │
└─────────────────────────────────────────────────────┘
```

#### 5. **Kế Thừa Cấu Trúc Này Có Nhất Quán Không?**

**Nhất quán hoàn toàn!** Vì:

1. **Cột `d_phuongthuc` rõ ràng**: người đọc code hay DB admin sẽ biết ngay đang xử lý phương thức nào
2. **Không gây nhầm lẫn**: không cần thêm bảng hay cột mới cho từng phương thức
3. **Linh hoạt cho mở rộng**: nếu sau này có phương thức mới, chỉ cần thêm giá trị cho `d_phuongthuc`, rồi dùng cột phù hợp

**Đặc biệt, so với cách tách thành nhiều bảng:**
- ❌ Tách THPT vào `xt_diemthpt`, VSAT vào `xt_diemvsat`, DGNL vào `xt_diemdgnl` → rối rắm, phải JOIN nhiều bảng
- ✅ Dùng `d_phuongthuc` + các cột chung → gọn gàng, dễ sửa code, dễ query

#### 6. **Lưu Ý Khi Code**

Khi viết code xử lý điểm, cần kiểm tra `d_phuongthuc`:

```java
DiemThiXetTuyen diem = diemDAO.getByCccd(cccd);

if ("THPT".equals(diem.getPhuongThuc())) {
    // Lấy điểm THPT từ TO, LI, HO
    BigDecimal diemMon1 = diem.getTo();
    BigDecimal diemMon2 = diem.getLi();
    BigDecimal diemMon3 = diem.getHo();
    // ... xử lý THPT
} else if ("V-SAT".equals(diem.getPhuongThuc())) {
    // Lấy điểm VSAT từ TO, LI, HO (hoặc các môn khác tùy tổ hợp)
    BigDecimal diemMon1 = diem.getTo();
    BigDecimal diemMon2 = diem.getLi();
    BigDecimal diemMon3 = diem.getHo();
    // ... xử lý VSAT, cần quy đổi
} else if ("DGNL".equals(diem.getPhuongThuc())) {
    // Lấy điểm DGNL từ NL1
    BigDecimal diemDgnl = diem.getNl1();
    // ... xử lý DGNL, cần quy đổi
}
```

---

## Câu Hỏi 5: Nội Suy Tuyến Tính Là Gì? Tác Dụng Gì?

### Định Nghĩa Toán Học

**Nội suy tuyến tính (Linear Interpolation)** là phương pháp ước tính **giá trị nằm giữa 2 điểm dữ liệu đã biết** bằng cách **nối chúng bằng một đường thẳng** và tính tung độ tại điểm cần tìm.

**Công thức toán học:**

$$y = c + \frac{x - a}{b - a} \times (d - c)$$

Trong đó:
- `x`: Giá trị đầu vào (cần tìm `y`)
- `[a, b]`: Khoảng xác định của `x` (điểm gốc)
- `[c, d]`: Khoảng giá trị tương ứng của `y` (điểm đích)
- `y`: Giá trị đầu ra (kết quả quy đổi)

### Giải Thích Từng Phần Công Thức

1. **Bước 1**: Tính **tỷ lệ vị trí** của `x` trong khoảng `[a, b]`
   ```
   ratio = (x - a) / (b - a)
   ```
   - Nếu `x = a` → `ratio = 0` (ở cận dưới)
   - Nếu `x = b` → `ratio = 1` (ở cận trên)
   - Nếu `x` ở giữa → `0 < ratio < 1`

2. **Bước 2**: Tính **độ chênh lệch** của khoảng đích
   ```
   delta = d - c
   ```

3. **Bước 3**: Áp dụng tỷ lệ vào khoảng đích
   ```
   y = c + ratio * delta
   ```

### Ví Dụ Cụ Thể

**Ví dụ 1: Quy Đổi Điểm VSAT Sang THPT**

Bảng quy đổi V-SAT Môn Toán:
```
Khoảng VSAT [114.5, 122.5] → Khoảng THPT [7, 7.75]
Khoảng VSAT [122.5, 130.5] → Khoảng THPT [7.75, 8.5]
```

**Trường hợp 1:** Điểm VSAT = 114.5 (cận dưới)
```
y = 7 + ((114.5 - 114.5) / (122.5 - 114.5)) * (7.75 - 7)
y = 7 + 0 * 0.75 = 7
```
→ Điểm THPT = 7 (tương ứng cận dưới)

**Trường hợp 2:** Điểm VSAT = 115 (ở giữa)
```
y = 7 + ((115 - 114.5) / (122.5 - 114.5)) * (7.75 - 7)
y = 7 + (0.5 / 8) * 0.75
y = 7 + 0.0625 * 0.75 = 7.047
```
→ Điểm THPT ≈ 7.047

**Trường hợp 3:** Điểm VSAT = 122.5 (cận trên)
```
y = 7 + ((122.5 - 114.5) / (122.5 - 114.5)) * (7.75 - 7)
y = 7 + 1 * 0.75 = 7.75
```
→ Điểm THPT = 7.75 (tương ứng cận trên)

### Lý Do Dùng Nội Suy Tuyến Tính Trong Dự Án

#### 1. **Công Bằng Trong Xét Tuyển**

Bộ GD&ĐT yêu cầu **bách phân vị tương đương** để đảm bảo:
- Thí sinh thi VSAT và THPT **cùng mức năng lực** → được **điểm quy đổi tương đương**
- Ví dụ: Nếu điểm VSAT 115 tương ứng **phân vị 20%** trên toàn bộ thí sinh VSAT
- Thì điểm THPT được quy đổi từ 115 cũng phải tương ứng **phân vị 20%** trên tổng thí sinh THPT

#### 2. **Liên Tục Và Trơn Tru**

- **Không dùng nội suy:** điểm VSAT 114.9 → 7, điểm VSAT 115 → 7.047 → nhảy cóc
- **Dùng nội suy:** điểm VSAT 114.9 → ~7.04, điểm VSAT 115 → 7.047 → mượt mà

#### 3. **Hỗ Trợ Bất Kỳ Điểm Nào Trong Khoảng**

- Bảng chỉ lưu điểm ở **phân vị nhất định** (3%, 10%, 20%, ..., 90%)
- Nội suy giúp tính được điểm trung gian trong bất kỳ khoảng nào

### So Sánh Với Các Phương Pháp Khác

| Phương Pháp | Ưu Điểm | Nhược Điểm |
|-----------|---------|-----------|
| **Nội suy tuyến tính** (hiện dùng) | Đơn giản, công bằng, liên tục | Giả sử quan hệ tuyến tính (không luôn đúng) |
| Nội suy đa thức bậc cao | Chính xác hơn | Phức tạp, dễ gây overfitting, có peak/valley |
| Khoảng cách cố định (bậc thang) | Rất đơn giản | Không công bằng, nhảy cóc |
| Phân nhóm điểm (Band) | Dễ hiểu | Khó xác định ranh giới, không công bằng |

**Kết luận:** Nội suy tuyến tính là **cân bằng tốt nhất** giữa đơn giản và chính xác.

### Tác Dụng Cụ Thể Trong Dự Án

1. **Quy Đổi Điểm VSAT → THPT:**
   - Giúp tính được điểm xét tuyển cho các thí sinh VSAT
   - Đảm bảo công bằng với thí sinh THPT

2. **Quy Đổi Điểm ĐGNL → THPT:**
   - Giúp tính được điểm xét tuyển cho các thí sinh ĐGNL
   - Đảm bảo công bằng với thí sinh THPT

3. **Quy Đổi Tổ Hợp Môn:**
   - Giúp thí sinh thi tổ hợp khác (không phải gốc) được so sánh công bằng
   - Ví dụ: Tổ hợp A01 quy về tổ hợp gốc A00

4. **Quy Đổi Chứng Chỉ Ngoại Ngữ:**
   - Giúp tính điểm cộng từ chứng chỉ tiếng Anh (IELTS, TOEFL, VSTEP)
   - Nội suy tuyến tính được dùng khi điểm chứng chỉ nằm **giữa 2 bản ghi** trong bảng

---

## Câu Hỏi 6: Quy Trình Nhập & Lưu Điểm - Hiểu Đúng Không?

### Tóm Tắt Kiến Thức

Bạn hiểu **hoàn toàn đúng**! ✅

**Quy trình chuẩn:**

1. **Giao diện `DiemThiXetTuyenGUI`:**
   - Dùng để **nhập điểm gốc** từ 3 phương thức (THPT/VSAT/DGNL)
   - Giữ nguyên dữ liệu gốc: THPT 0-10, VSAT 0-150, DGNL 0-1200
   - **Lưu vào bảng `xt_diemthixettuyen`**

2. **Giao diện `BangQuyDoiGUI`:**
   - Dùng để **quản lý bảng tra quy đổi** (không phải nhập điểm trực tiếp)
   - Nhập các mốc quy đổi `[a, b] → [c, d]` cho từng phương thức, tổ hợp, môn
   - **Lưu vào bảng `xt_bangquydoi`**

3. **Giao diện `DiemCongXetTuyenGUI`:**
   - Dùng để **nhập/tính điểm cộng** (ĐC + ĐUT)
   - Hiển thị kết quả tính: tổng ĐC (tối đa 3.0 trên thang 30)

4. **Giao diện `NguyenVongXetTuyenGUI`:**
   - Dùng để **xem kết quả xét tuyển** (sau khi quy đổi)
   - Bấm nút "Xét Tuyển" → hệ thống tự động:
     - Tra bảng `xt_bangquydoi`
     - Quy đổi điểm VSAT/DGNL & điểm tổ hợp
     - Tính ĐXT = ĐTHGXT + ĐC + ĐUT
     - Lưu vào bảng `xt_nguyenvongxettuyen`

### Chi Tiết Từng Giai Đoạn

#### **Giai Đoạn 1: Nhập Điểm Gốc** (DiemThiXetTuyenGUI)

**Input:** File Excel hoặc import từ API có chứa:
```
CCCD | Phương thức | TO | LI | HO | SI | ... | NL1
0001 | THPT        | 8  | 7  | 9  | -  | ... | -
0002 | V-SAT       | 125| 118| 110| -  | ... | -
0003 | DGNL        | -  | -  | -  | -  | ... | 850
```

**Output:** Lưu vào `xt_diemthixettuyen`
```sql
INSERT INTO xt_diemthixettuyen (cccd, d_phuongthuc, TO, LI, HO, ...)
VALUES ('0001', 'THPT', 8, 7, 9, ...);
```

**Đặc điểm:**
- Điểm lưu **nguyên gốc**, chưa quy đổi
- Cột `d_phuongthuc` bắt buộc phải có để xác định loại phương thức
- Các cột không dùng của phương thức đó để NULL

---

#### **Giai Đoạn 2: Quản Lý Bảng Quy Đổi** (BangQuyDoiGUI)

**Input:** Quy định từ Bộ GD&ĐT (ở folder `data`)
```
Ví dụ từ "Quy doi diem thi V-SAT 2025.txt":
Khoảng VSAT: (114.5, 122.5] → Khoảng THPT: (7, 7.75] (phân vị 20%)
```

**Output:** Lưu vào `xt_bangquydoi`
```sql
INSERT INTO xt_bangquydoi 
(d_phuongthuc, d_tohop, d_mon, d_diema, d_diemb, d_diemc, d_diemd, d_phanvi)
VALUES ('V-SAT', NULL, 'TO', 114.5, 122.5, 7, 7.75, '20%');
```

**Đặc điểm:**
- Bảng này chỉ là **dữ liệu cấu hình**, không thay đổi thường xuyên
- Người quản trị nhập **một lần** khi setup hệ thống
- Code (BUS layer) sẽ tra bảng này khi tính toán

---

#### **Giai Đoạn 3: Nhập Điểm Cộng** (DiemCongXetTuyenGUI)

**Input:** Dữ liệu từ Sơ yếu lý lịch thí sinh
```
Chứng chỉ Tiếng Anh: IELTS 6.5 → Quy đổi 1.5 điểm
Giải thí sinh giỏi: Quốc gia B → Cộng 1.0 điểm
Ưu tiên khu vực: Miền núi → MĐUT = 0.75 điểm
```

**Output:** Lưu vào `xt_diemcongxetuyen`
```sql
INSERT INTO xt_diemcongxetuyen 
(ts_cccd, diemCC, diemUtqd, diemTong)
VALUES ('0001', 1.5, 0.75, 2.25); -- ĐC + ĐUT
```

**Đặc điểm:**
- Giao diện này **có thể tính tự động** từ bảng quy đổi chứng chỉ
- Hoặc **nhập thủ công** nếu quản trị viên có dữ liệu rõ ràng

---

#### **Giai Đoạn 4: Xét Tuyển & Lưu Kết Quả** (NguyenVongXetTuyenGUI)

**Workflow:**

```
1. Lấy dữ liệu điểm thí sinh từ xt_diemthixettuyen
   ↓
2. Đọc danh sách nguyện vọng từ xt_nguyenvongxettuyen (chưa có kết quả)
   ↓
3. Với mỗi nguyện vọng:
   a) Tra bảng xt_bangquydoi
      - Nếu VSAT: quy đổi 3 môn từ thang 150 về thang 10
      - Nếu DGNL: quy đổi NL1 từ thang cao về thang 30
      - Nếu THPT: không cần quy đổi phương thức, nhưng quy đổi tổ hợp nếu khác gốc
   
   b) Tính ĐTHXT (tổng điểm 3 môn theo hệ số)
   
   c) Quy đổi về tổ hợp gốc (nếu cần)
   
   d) Cộng ĐC từ xt_diemcongxetuyen
   
   e) Tính ĐUT theo quy tắc giảm dần
   
   f) Tính ĐXT = ĐTHGXT + ĐC + ĐUT
   
   g) So sánh ĐXT với điểm chuẩn của ngành
      - Nếu ĐXT ≥ điểm chuẩn AND chỉ tiêu > 0 → ĐỖ, dừng
      - Nếu không → TRƯỢT, tiếp tục NV tiếp theo
   
   h) Lưu ĐXT & kết quả vào xt_nguyenvongxettuyen
   
4. Tất cả NV chưa được xét → gắn tag "KHÔNG XÉT"
```

**Output:** Cập nhật `xt_nguyenvongxettuyen` với kết quả xét tuyển

---

### Câu Hỏi Chi Tiết: "Bảng QuyDoi Chỉ Nên Có VSAT & DGNL Thôi, Không Có THPT?"

**Trả lời: Đúng!** ✅

#### **Tại Sao Không Nên Có Quy Đổi THPT?**

1. **THPT Không Cần Quy Đổi Phương Thức:**
   - Điểm THPT đã là thang 10 chuẩn
   - Không có "thang điểm THPT cao" để quy về "thang điểm THPT thấp"
   - Việc quy đổi THPT → THPT là vô nghĩa

2. **Quy Đổi Tổ Hợp Không Nằm Trong BangQuyDoi:**
   - Quy đổi tổ hợp (A00 → D01) dùng **bảng độ lệch**, không phải bảng bách phân vị
   - Quy tắc riêng trong file `cac cong thuc tinh.txt`
   - Nên xử lý **riêng biệt** trong code, không pha lẫn với BangQuyDoi

#### **BangQuyDoi Chỉ Nên Chứa:**

```
✅ V-SAT (tất cả 8 môn): thang 150 → thang 10
✅ DGNL (theo tổ hợp): thang cao (1200 hoặc khác) → thang 30
❌ THPT → THPT (vô lý)
❌ THPT → VSAT (không có cách quy đổi ngược)
❌ VSAT → DGNL (hai phương thức khác nhau, không so sánh được)
```

#### **Ví Dụ Dữ Liệu Hợp Lý:**

```sql
-- ✅ HỢP LỆ
INSERT INTO xt_bangquydoi 
(d_phuongthuc, d_mon, d_diema, d_diemb, d_diemc, d_diemd, d_phanvi)
VALUES ('V-SAT', 'TO', 114.5, 122.5, 7, 7.75, '20%');
VALUES ('V-SAT', 'LI', 110.5, 118.5, 6.5, 7.5, '20%');
VALUES ('DGNL', 'A01', 800, 900, 20, 25, '20%');

-- ❌ KHÔNG HỢP LỆ
INSERT INTO xt_bangquydoi 
(d_phuongthuc, d_mon, d_diema, d_diemb, d_diemc, d_diemd, d_phanvi)
VALUES ('THPT', 'TO', 7, 10, 7, 10, '100%');  -- Không cần quy đổi THPT → THPT
VALUES ('V-SAT', 'TO', 7, 10, 114.5, 122.5, '20%');  -- Sai chiều quy đổi (ngược lại)
```

---

### Kết Luận

**Quy Trình Chính Xác:**

```
📥 Nhập Điểm Gốc (DiemThiXetTuyenGUI)
   ↓ Lưu: xt_diemthixettuyen
   
📋 Quản Lý Bảng Quy Đổi (BangQuyDoiGUI)
   ↓ Lưu: xt_bangquydoi (VSAT & DGNL chỉ)
   
➕ Nhập Điểm Cộng (DiemCongXetTuyenGUI)
   ↓ Lưu: xt_diemcongxetuyen
   
🔄 Xét Tuyển - Tự Động Quy Đổi (NguyenVongXetTuyenGUI)
   ├─ Tra xt_bangquydoi → quy đổi VSAT/DGNL
   ├─ Quy đổi tổ hợp (riêng, không dùng BangQuyDoi)
   ├─ Tính ĐXT = ĐTHGXT + ĐC + ĐUT
   └─ Lưu: xt_nguyenvongxettuyen (với kết quả xét tuyển)
```

**Bảng BangQuyDoi là:** Dữ liệu cấu hình bách phân vị, **chỉ dùng cho VSAT & DGNL**, không bao gồm THPT.
