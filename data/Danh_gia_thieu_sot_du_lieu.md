# Đánh giá và Phân tích Cấu trúc Dữ liệu - Dự án Tuyển sinh

Sau khi phân tích toàn bộ các file text chứa trong thư mục `data` và cấu trúc CSDL được mô tả trong file `Dự án phần mềm tuyển sinh.txt`, dưới đây là danh sách những điểm bất hợp lý và khiếm khuyết trong dữ liệu:

## 1. Sự bất hợp lý giữa CSDL và Dữ liệu thực tế (`Dự án phần mềm tuyển sinh.txt`)
- **Tên cột không nhất quán:** Trong bảng `xt_nguyenvongxettuyen`, cột lưu mã định danh của thí sinh được đặt tên là `nn_cccd`. Trong khi đó, các bảng khác như `xt_diemcongxetuyen` hoặc `xt_thisinhxettuyen25` lại dùng `ts_cccd`. Sự thiếu đồng bộ này dễ gây nhầm lẫn khi thực hiện các phép `JOIN` trong CSDL.
- **Giá trị mẫu (Sample Data) chưa hợp lệ:** Ở bảng `xt_nguyenvongxettuyen`, cột `nv_ketqua` chứa chuỗi định dạng là `duolaar` thay vì các trạng thái đánh giá mong đợi như: `Đậu/Rớt`, `Pass/Fail` hay `1/0`.
- **Độ dài biến không đồng nhất:** Cột `manganh` trong bảng `xt_diemcongxetuyen` định nghĩa là `VARCHAR(20)`. Tuy nhiên, trong bảng `xt_nganh` gốc thì `manganh` lại là `VARCHAR(45)`. Bên cạnh đó, cột `dc_keys` sinh ra bằng cách ghép chuỗi (VD: `056307010216_7340101_B00`) có khả năng vượt quá giới hạn nếu mã ngành hoặc mã tổ hợp quá dài.
- **Thiếu cột môn học khai báo:** File dữ liệu `Ds thi sinh.txt` xuất hiện điểm số cho môn `GDCD` (Giáo dục công dân) và các môn năng khiếu từ `NK1` đến `NK10`. Tuy nhiên, CSDL hiện tại (bảng `xt_diemthixettuyen`) chỉ có cột `NK1`, `NK2` và không hề có cột nào để lưu điểm môn `GDCD`, `GD`, hay các môn `NK3 -> NK10`.

## 2. File `Chi tieu 2025.txt`
- **Bước nhảy STT không liên tục:** Cột Số thứ tự (STT) nhảy cóc từ `44` sang `46`, bị thiếu mất dòng số `45`.
- **Headers bị lỗi do Merge Cells:** Dòng tiêu đề bị dư hoặc thiếu tên dẫn đến xuất hiện các cột rác `Unnamed: 0`, `Unnamed: 2`, `Unnamed: 3`.
- **Dữ liệu tổng kết ở cuối file:** Các dòng cuối cùng (dòng 51 đổ xuống) lưu số lượng tổng cộng (`5213`) hoặc chứa chuỗi thay vì dữ liệu từng ngành cụ thể, trong khi các cột khác lại chứa `NaN`. Khi import tự động vào hệ thống, các dòng này sẽ gây lỗi SQL hoặc Type Mismatch.

## 3. File `tohopmon.txt`
- **Lạm dụng chuỗi `NaN`:** Các cột như `tb_keys`, `Gốc` hoặc `Độ lệch` xuất hiện rất nhiều giá trị chuỗi văn bản `"NaN"`. Giá trị này không nên tồn tại trong file dạng import; thay vì ghi `"NaN"`, nên để thành chuỗi rỗng (`""`), giá trị Null, hoặc đối với cột số học (`Độ lệch`) thì để là `0`. Việc để chuỗi `NaN` sẽ làm vỡ luồng ép kiểu (Float/Decimal) trong Java.

## 4. File `Ds quy doi tieng Anh.txt`
- Tương tự như file chỉ tiêu, dòng header của file này chèn lẫn các ô bị lỗi gộp (`Unnamed: 6`, `Unnamed: 7`).
- Các cột `Điểm cộng` hoặc `Điểm Quy đổi` bị xen kẽ các chữ `NaN`, dẫn đến rủi ro sập ứng dụng (Exception) nếu phần mềm parser cố gắng chuyển đổi chuỗi chữ sang kiểu `double/float`.

## 5. File `Nguyenvong.txt`
- **Tên file dễ gây nhầm lẫn và thiếu dữ liệu thô:** Theo như yêu cầu của Chức năng 8 (Quản lý nguyện vọng và xét tuyển), phần mềm cần dữ liệu raw (các nguyện vọng của từng thí sinh) để tính điểm và lọc trúng tuyển. Tuy nhiên, nội dung trong file `Nguyenvong.txt` lại là một **báo cáo thống kê** (tính tổng quan số lượng nguyện vọng cho từng ngành). Dự án hiện tại **không có dữ liệu gốc** về danh sách nguyện vọng để import cho quá trình xét tuyển.

## 6. File `Ds thi sinh.txt` và `Uu tien xet tuyen.txt`
- Cột mã ngoại ngữ có giá trị như `N1` và điểm số ở cột `NN`. Nhưng bảng `xt_diemthixettuyen` lại chia thành `N1_THI`, `N1_CC`, vân vân. Cần làm rõ mapping giữa cột trong file và hệ thống CSDL.
- Tương tự, file chứa cực kỳ nhiều ô ghi thành chữ `NaN` cho các điểm số không có. File `Uu tien xet tuyen.txt` chứa nhiều sheets giả lập nhưng xuất ra file txt bị lẫn tiêu đề phụ (như `--- Sheet: ds thi sinh ---`), kèm theo các headers gộp (Cấp, Loại giải, ĐT) rất khó để dùng code máy móc (Auto Parser) đọc nếu không format lại.

## 7. Các tệp dữ liệu quy đổi (V-SAT, TA, ĐGNL)
- Các file như `Quy doi diem thi V-SAT 2025.txt`, `PLuc_Bang bach phan vi ... .txt` và `bangQUyDoiTA_2025.txt` đang được format dưới dạng văn bản phục vụ con người đọc (human-readable, trình bày thành báo cáo, khoảng điểm `a < x <= b`, text phụ lục).
- **Vấn đề cốt lõi:** Chức năng 9 yêu cầu *Import danh sách* vào bảng cấu hình quy đổi (`xt_bangquydoi`). Để có thể lấy vào CSDL, dữ liệu cần có cấu trúc Flat-File chuẩn (ví dụ CSV / Excel thuần list). Dữ liệu dạng document hiện tại đòi hỏi phải tự hardcode logic nội suy, hoặc người quản trị phải rà soát, viết lại file Excel chuẩn trước khi import vào database.

## 8. Thiếu phần chuyển điểm THPT sang điểm xét tuyển THPT
- **Khuyết điểm nghiêm trọng:** Dữ liệu hiện tại chủ yếu tập trung vào việc quy đổi điểm năng lực (V-SAT, ĐGNL) về thang điểm THPT hoặc quy đổi bài thi Tiếng Anh. Tuy nhiên, hệ thống lại **hoàn toàn thiếu vắng** tệp dữ liệu, bảng bách phân vị, hoặc quy định chi tiết để chuẩn hóa/quy đổi trực tiếp từ **Điểm thi THPT** sang **Điểm xét tuyển THPT (ĐTHXT_THPT)** trong trường hợp môn thi/tổ hợp có yêu cầu đặc thù hoặc cần chuyển đổi thang điểm. 
- Mặc dù có công thức chung ở `cac cong thuc tinh.txt` nhưng nếu hệ thống cho phép lưu thông tin quy đổi của mọi phương thức vào chung một bảng `xt_bangquydoi` thì sự vắng mặt của dữ liệu mapping cơ bản cho chính phương thức THPT là một thiếu sót lớn. Sự khuyết thiếu này làm cho cấu trúc CSDL không có dữ liệu khởi tạo đồng bộ.

---

## 9. 🚨 CÁC LỖ HỔNG NGHIÊM TRỌNG (CRITICAL VULNERABILITIES) LÀM SỤP ĐỔ LOGIC HỆ THỐNG
Sau khi rà soát chéo giữa các quy tắc toán học, cơ sở dữ liệu và dữ liệu thô (.txt), dưới đây là 3 lỗ hổng chí mạng sẽ khiến thuật toán xét tuyển (Chức năng số 8) không thể hoạt động hoặc trả kết quả sai lệch hoàn toàn:

### 9.1 Lỗ hổng Tràn Điểm do Mâu Thuẫn Thang Điểm Cộng (Logic Flaw)
- Sự đối lập: Bảng quy đổi Tiếng Anh (`bangQUyDoiTA_2025.txt`) yêu cầu cộng điểm chứng chỉ trực tiếp vào mức điểm gốc của từng kỳ thi: **ĐGNL (+40, 60, 80 điểm)**, **V-SAT (+15, 22.5, 30 điểm)**. 
- Tuy nhiên, Hướng dẫn ở `cac cong thuc tinh.txt` quy định: **Tất cả ĐTHXT phải quy về thang 30 trước** >> Sau đó mới cộng Điểm Ưu Tiên (ĐUT) và Điểm Cộng (ĐC) với điều kiện là **Tổng ĐC không vượt quá 3 điểm**. (`ĐXT = ĐTHGXT + ĐC + ĐUT`).
- **Hậu quả:** Nếu bộ phận phát triển code rập khuôn lấy điểm Điểm Cộng (80 của ĐGNL) cộng vào công thức thang 30 điểm, điểm số của thí sinh sẽ vượt ngưỡng > 100 điểm trên thang 30. Lỗ hổng đặc tả này cực kỳ nguy hiểm, thiếu bước yêu cầu chuẩn hoá các loại ĐC về thang 30 trước khi thực hiện quy tắc tổng hoặc chia tỷ lệ.

### 9.2 Lỗ hổng Bốc Hơi Dữ Liệu Nguyện Vọng Cốt Lõi (Missing Core Data)
- **Tình trạng:** Khảo sát trong `Ds thi sinh.txt` hệ thống có đến khoảng **49.400** records dữ liệu điểm. Tuy nhiên, toàn bộ project không có file dữ liệu raw biểu thị **Thứ tự Cụ Thể Từng Nguyện Vọng** của số thí sinh này. Tệp `Nguyenvong.txt` cung cấp thực chất chỉ là "Báo cáo thống kê tổng cộng số lượng nguyện vọng theo từng ngành" (Dữ liệu đã được group by tổng hợp), không phải dữ liệu chi tiết của từng sinh viên. Tệp detail duy nhất nằm ở sheet của `Uu tien xet tuyen.txt` nhưng chỉ cover khoảng ~300 thí sinh diện đặc biệt.
- **Hậu quả:** Không có dữ liệu đầu vào `nv_thutu`, thuật toán lọc trúng tuyển toàn trường sẽ tịt ngòi, không thể giả lập kết quả thực tiễn cho tất cả ứng viên.

### 9.3 Lỗ hổng Tranh Chấp Chỉ Tiêu (Quota Allocation Defect)
- Cấu trúc database ở bảng `xt_nganh` định nghĩa rõ ràng về phân bổ chỉ tiêu cho từng phương thức rẽ nhánh: Tuyển thẳng (`sl_xtt`), ĐGNL (`sl_dgnl`), V-SAT (`sl_vsat`), THPT (`sl_thpt`).
- **Tình trạng thiếu sót:** Dữ liệu khởi tạo trong `Chi tieu 2025.txt` chỉ cho **1 con số Tổng Chỉ Tiêu** duy nhất (Ví dụ: Ngành Sư Phạm Ngữ Văn là 50 slot). Hoàn toàn không có dữ liệu tỉ lệ % hay con số slot cụ thể chia cho các phương thức.
- **Hậu quả:** Khi thuật toán lấp đầy danh sách trúng tuyển từ điểm cao xuống điểm thấp, việc khuyết data chặn trên (Quota limit) của từng phương thức sẽ khiến hệ thống không thể so sánh và dừng lấy thí sinh đúng lúc. Đây là một lỗ hổng vận hành trầm trọng.
