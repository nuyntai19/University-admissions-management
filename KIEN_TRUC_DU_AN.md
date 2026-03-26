# Tài Liệu Kiến Trúc - Hệ Thống Quản Lý Tuyển Sinh

## 📋 Mục Đích Dự Án

Xây dựng hệ thống quản lý tuyển sinh toàn diện cho Trường Đại học Su-Vet (SGU), hỗ trợ:

- Quản lý người dùng và phân quyền
- Quản lý thí sinh & ngành tuyển sinh
- Cấu hình tổ hợp môn & điểm thi
- Tính điểm xét tuyển & quản lý nguyện vọng
- Nhập dữ liệu từ Excel (Import feature)

## 🏗️ Kiến Trúc Chi Tiết

### **3-Tier Architecture**

```
┌─────────────────────────────────────┐
│  GUI Layer (Swing Components)       │
│  - JFrame, JPanel, JTable, JButton  │
│  - User Interaction                 │
└──────────────┬──────────────────────┘
               │ Calls
┌──────────────▼──────────────────────┐
│  BUS Layer (Business Logic)         │
│  - Validation & Rules               │
│  - Authentication                   │
│  - Data Processing                  │
└──────────────┬──────────────────────┘
               │ Uses
┌──────────────▼──────────────────────┐
│  DAO Layer (Data Access Object)     │
│  - CRUD Operations                  │
│  - Query Building                   │
│  - Transaction Management           │
└──────────────┬──────────────────────┘
               │ Hibernate ORM
┌──────────────▼──────────────────────┐
│  Database (MySQL)                   │
│  - xettuyen2026 database            │
│  - 9 tables + auxiliary tables      │
└─────────────────────────────────────┘
```

## 📁 Cấu Trúc Thư Mục

```
PhanMemTuyenSinh/
│
├── pom.xml                           # Maven configuration
├── HUONG_DAN_CHAY.md                 # Running instructions (Vietnamese)
│
├── src/main/
│   ├── java/vn/edu/sgu/phanmemtuyensinh/
│   │   ├── PhanMemTuyenSinh.java     # Main entry point (JFrame + JTabbedPane)
│   │   │
│   │   ├── dal/                      # Data Access Layer
│   │   │   ├── entity/
│   │   │   │   ├── NguoiDung.java
│   │   │   │   ├── ThiSinh.java
│   │   │   │   ├── Nganh.java
│   │   │   │   ├── NganhToHop.java
│   │   │   │   ├── DiemThiXetTuyen.java
│   │   │   │   ├── DiemCongXetTuyen.java
│   │   │   │   ├── NguyenVongXetTuyen.java
│   │   │   │   ├── BangQuyDoi.java
│   │   │   │   └── ToHopMon.java
│   │   │   ├── NguoiDungDAO.java
│   │   │   ├── ThiSinhDAO.java
│   │   │   ├── NganhDAO.java
│   │   │   ├── NganhToHopDAO.java
│   │   │   ├── DiemThiXetTuyenDAO.java
│   │   │   ├── DiemCongXetTuyenDAO.java
│   │   │   ├── NguyenVongXetTuyenDAO.java
│   │   │   ├── BangQuyDoiDAO.java
│   │   │   └── ToHopMonDAO.java
│   │   │
│   │   ├── bus/                      # Business Logic Layer
│   │   │   ├── NguoiDungBUS.java
│   │   │   ├── ThiSinhBUS.java
│   │   │   ├── NganhBUS.java
│   │   │   ├── NganhToHopBUS.java
│   │   │   ├── DiemThiXetTuyenBUS.java
│   │   │   ├── DiemCongXetTuyenBUS.java
│   │   │   ├── NguyenVongXetTuyenBUS.java
│   │   │   ├── BangQuyDoiBUS.java
│   │   │   └── ToHopMonBUS.java
│   │   │
│   │   ├── gui/                      # Presentation Layer (Swing)
│   │   │   ├── DashboardGUI.java
│   │   │   ├── NguoiDungGUI.java
│   │   │   ├── ThiSinhGUI.java
│   │   │   ├── NganhGUI.java
│   │   │   ├── NganhToHopGUI.java
│   │   │   ├── DiemThiXetTuyenGUI.java
│   │   │   ├── DiemCongXetTuyenGUI.java
│   │   │   ├── NguyenVongXetTuyenGUI.java
│   │   │   ├── BangQuyDoiGUI.java
│   │   │   └── ToHopMonGUI.java
│   │   │
│   │   └── utils/
│   │       └── HibernateUtil.java
│   │
│   └── resources/
│       └── hibernate.cfg.xml          # Hibernate configuration (DB connection)
│
├── database/
│   ├── xettuyen2026_empty.sql
│   └── xettuyen2026_v2_with_users.sql
│
└── target/                            # Build output (generated)
    ├── classes/
    └── ...
```

## 📊 Entity Relationship Diagram (ERD)

```
[xt_nguoidung]
    │ (admin/user roles)
    │
[Quản lý người dùng & phân quyền]

[xt_thisinhxettuyen25]
    │ (CCCD - unique)
    ├──→ [xt_diemthixettuyen] (exam scores)
    ├──→ [xt_diemcongxettuyen] (bonus points)
    └──→ [xt_nguyenvongxettuyen]
            │
            └──→ [xt_nganh] (Programs)
                    │
                    └──→ [xt_nganh_tohop]
                            │
                            └──→ [xt_tohopmon] (Subject combinations)

[xt_bangquydoi] (Score conversion table)
    └──→ Referenced by [xt_diemthixettuyen]
```

## 🎯 Module & Features

| #   | Module           | Tính Năng                                   |
| --- | ---------------- | ------------------------------------------- |
| 1   | **Dashboard**    | Thống kê tổng quan 9 module, stat cards     |
| 2   | **Người Dùng**   | CRUD users, role management (admin/user)    |
| 3   | **Thí Sinh**     | CRUD students, search by name/CCCD          |
| 4   | **Ngành**        | CRUD programs, quota & score settings       |
| 5   | **Ngành-Tổ Hợp** | Map programs to subject combos with weights |
| 6   | **Điểm Thi**     | CRUD exam scores (THPT/VSAT/ĐGNL)           |
| 7   | **Điểm Cộng**    | CRUD bonus/priority points                  |
| 8   | **Nguyện Vọng**  | Track preferences & admission results       |
| 9   | **Bảng Quy Đổi** | Score conversion tiers (A/B/C/D)            |
| 10  | **Tổ Hợp Môn**   | CRUD subjects, **Excel import feature**     |

## 🔐 Authentication & Authorization

### NguoiDungBUS.authenticate(taiKhoan, matkhau)

- Kiểm tra credentials từ database
- Trả về user object hoặc null
- Được gọi từ login screen hoặc NguoiDungGUI

### Phân Quyền (phanQuyen)

- `admin` - Full access tất cả modules
- `user` - Limited access (configurable)

## 🗄️ Database Schema Highlights

### Table: xt_nguoidung (User Management)

```sql
CREATE TABLE xt_nguoidung (
    idnguoidung INT AUTO_INCREMENT PRIMARY KEY,
    taikhoan VARCHAR(45) UNIQUE NOT NULL,
    matkhau VARCHAR(255) NOT NULL,
    phanquyen VARCHAR(45) DEFAULT 'user',  -- admin|user
    trangthaihoatdong TINYINT DEFAULT 1,   -- 1=active, 0=inactive
    hoTen VARCHAR(100),
    email VARCHAR(100),
    dienThoai VARCHAR(20)
);
```

### Table: xt_nganh_tohop (Subject Mapping)

```sql
CREATE TABLE xt_nganh_tohop (
    id INT AUTO_INCREMENT PRIMARY KEY,
    maNganh VARCHAR(10) NOT NULL,
    maToHop VARCHAR(10) NOT NULL,
    hsmon1 FLOAT,  -- subject 1 weight
    hsmon2 FLOAT,  -- subject 2 weight
    hsmon3 FLOAT,  -- subject 3 weight
    ... (flags for N1, TO, LI, etc.)
    FOREIGN KEY (maNganh) REFERENCES xt_nganh(maNganh),
    FOREIGN KEY (maToHop) REFERENCES xt_tohopmon(maToHop)
);
```

## 🧩 Design Patterns Used

### 1. **Singleton Pattern** (HibernateUtil)

```java
private static SessionFactory sessionFactory;

static {
    sessionFactory = new MetadataSources(registry)
        .buildMetadata()
        .buildSessionFactory();
}

public static SessionFactory getSessionFactory() {
    return sessionFactory;
}
```

### 2. **DAO Pattern** (Data Access)

- Each entity has corresponding DAO class
- Encapsulates DB operations (getAll, getById, add, update, delete)
- Uses Hibernate Session for transaction management

### 3. **MVC Pattern** (GUI + BUS + DAO)

- GUI (View) - User interaction
- BUS (Controller) - Logic & validation
- DAO (Model) - Data persistence

## 📦 Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.4.4.Final</version>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>

<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

## 🚀 Deployment

### Development

```bash
mvn exec:java -Dexec.mainClass="vn.edu.sgu.phanmemtuyensinh.PhanMemTuyenSinh"
```

### Production

```bash
mvn clean package
java -jar target/PhanMemTuyenSinh-1.0-SNAPSHOT.jar
```

## 📝 Coding Standards

- **Package Structure**: `vn.edu.sgu.phanmemtuyensinh.{dal,bus,gui,utils}`
- **Naming Conventions**:
  - Entity classes: PascalCase (e.g., `ThiSinh`)
  - DAO classes: Suffix `DAO` (e.g., `ThiSinhDAO`)
  - BUS classes: Suffix `BUS` (e.g., `ThiSinhBUS`)
  - GUI classes: Suffix `GUI` (e.g., `ThiSinhGUI`)
  - Variable names: camelCase (e.g., `currentId`)

## 🔄 Data Flow Example

**Adding a new student:**

```
ThiSinhGUI.themThiSinh()
    → Create ThiSinh object + populate fields
    → Call ThiSinhBUS.add(thisinh)
        → Validate input (non-null checks)
        → Call ThiSinhDAO.add(thisinh)
            → beginTransaction()
            → session.save(entity)
            → commit()
    → Refresh table (loadDuLieu())
    → Clear form (lamMoi())
```

## 🛠️ Future Enhancements

1. **Login Screen** - Implement authentication before main app
2. **Report Generation** - Export admissions to PDF/Excel
3. **Scoring Algorithm** - Automated score calculation based on weights
4. **Batch Import** - Import multiple modules at once
5. **API Layer** - REST API for mobile app integration
6. **Database Backup** - Automated backup scheduler
7. **Audit Logging** - Track user actions & changes

## 📞 Support & Maintenance

- Database backups: Weekly recommended
- Log rotation: Configure in logging utility
- Performance tuning: Monitor slow queries in console output
- Security: Hash passwords in database (bcrypt recommended)

---

**Documentation Version**: 1.0  
**Last Updated**: 2024  
**Status**: ✅ Complete Design
