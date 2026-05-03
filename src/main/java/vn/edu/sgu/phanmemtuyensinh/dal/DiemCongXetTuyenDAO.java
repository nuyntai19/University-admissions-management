package vn.edu.sgu.phanmemtuyensinh.dal;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;
import vn.edu.sgu.phanmemtuyensinh.utils.HibernateUtil;

public class DiemCongXetTuyenDAO {

    /**
     * Lấy toàn bộ danh sách điểm cộng từ database
     */
    public List<DiemCongXetTuyen> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DiemCongXetTuyen", DiemCongXetTuyen.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<DiemCongXetTuyen> getPage(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DiemCongXetTuyen", DiemCongXetTuyen.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long countAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT count(d) FROM DiemCongXetTuyen d", Long.class).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public List<DiemCongXetTuyen> getPage(String keyword, int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getPage(offset, limit);
            }
            return session.createQuery("FROM DiemCongXetTuyen WHERE tsCccd LIKE :kw OR chungChi LIKE :kw", DiemCongXetTuyen.class)
                    .setParameter("kw", "%" + keyword.trim() + "%")
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long countAll(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return countAll();
            }
            return session.createQuery("SELECT count(d) FROM DiemCongXetTuyen d WHERE tsCccd LIKE :kw OR chungChi LIKE :kw", Long.class)
                    .setParameter("kw", "%" + keyword.trim() + "%")
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
    * Lấy một bản ghi điểm cộng duy nhất dựa trên ID
    * @param idDiemCong ID cần tìm
    * @return Đối tượng DiemCongXetTuyen hoặc null nếu không tìm thấy
    */
   public DiemCongXetTuyen getById(int idDiemCong) {
       try (Session session = HibernateUtil.getSessionFactory().openSession()) {
           // session.get sẽ trả về null nếu ID không tồn tại trong DB
           return session.get(DiemCongXetTuyen.class, idDiemCong);
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }
   }

    /**
     * Tìm kiếm CCCD từ bảng thí sinh để phục vụ chức năng Suggestions trên GUI
     * Lưu ý: "ThiSinhXetTuyen25" là tên Class Entity tương ứng với bảng thí sinh của bạn
     */
    public List<ThiSinh> searchThiSinh(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Tìm theo CCCD hoặc Số báo danh
            String hql = "FROM ThiSinh t WHERE t.cccd LIKE :kw OR t.soBaoDanh LIKE :kw";
            return session.createQuery(hql, ThiSinh.class)
                          .setParameter("kw", keyword + "%")
                          .setMaxResults(10)
                          .list();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public boolean saveOrUpdate(DiemCongXetTuyen diem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Tìm theo dc_keys (unique key) thay vì tsCccd
            // Cho phép 1 CCCD có nhiều bản ghi (1 cho CC tiếng Anh, 1 cho giải thưởng)
            String hql = "FROM DiemCongXetTuyen d WHERE d.dcKeys = :key";
            List<DiemCongXetTuyen> list = session.createQuery(hql, DiemCongXetTuyen.class)
                    .setParameter("key", diem.getDcKeys())
                    .list();
                    
            if (!list.isEmpty()) {
                DiemCongXetTuyen existing = list.get(0);
                
                // --- Luôn cập nhật điểm CC nếu incoming > 0 ---
                if (diem.getDiemCC() != null && diem.getDiemCC().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    existing.setDiemCC(diem.getDiemCC()); 
                }
                // --- Luôn cập nhật điểm UT nếu incoming > 0 ---
                if (diem.getDiemUtxt() != null && diem.getDiemUtxt().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    existing.setDiemUtxt(diem.getDiemUtxt()); 
                }

                // --- Luôn cập nhật metadata chứng chỉ ngoại ngữ (nếu có) ---
                if (diem.getChungChi() != null && !diem.getChungChi().isEmpty())
                    existing.setChungChi(diem.getChungChi());
                if (diem.getMucDatDuoc() != null && !diem.getMucDatDuoc().isEmpty())
                    existing.setMucDatDuoc(diem.getMucDatDuoc());
                if (diem.getDiemQuyDoiChungChi() != null)
                    existing.setDiemQuyDoiChungChi(diem.getDiemQuyDoiChungChi());

                // --- Luôn cập nhật metadata giải thưởng (nếu có) ---
                if (diem.getCapGiai() != null && !diem.getCapGiai().isEmpty())
                    existing.setCapGiai(diem.getCapGiai());
                if (diem.getDoiTuongGiai() != null && !diem.getDoiTuongGiai().isEmpty())
                    existing.setDoiTuongGiai(diem.getDoiTuongGiai());
                if (diem.getMaMonGiai() != null && !diem.getMaMonGiai().isEmpty())
                    existing.setMaMonGiai(diem.getMaMonGiai());
                if (diem.getLoaiGiai() != null && !diem.getLoaiGiai().isEmpty())
                    existing.setLoaiGiai(diem.getLoaiGiai());
                if (diem.getDiemCongMonGiai() != null)
                    existing.setDiemCongMonGiai(diem.getDiemCongMonGiai());
                if (diem.getDiemCongKhongMon() != null)
                    existing.setDiemCongKhongMon(diem.getDiemCongKhongMon());

                // --- Cập nhật co_chung_chi ---
                if (diem.getCoChungChi() != null)
                    existing.setCoChungChi(diem.getCoChungChi());
                
                // --- Tính lại tổng ---
                java.math.BigDecimal tong = java.math.BigDecimal.ZERO;
                if (existing.getDiemCC() != null) tong = tong.add(existing.getDiemCC());
                if (existing.getDiemUtxt() != null) tong = tong.add(existing.getDiemUtxt());
                existing.setDiemTong(tong);
                
                session.merge(existing);
            } else {
                session.persist(diem);
            }
            
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try { transaction.rollback(); } catch (Exception ignored) {}
            }
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean add(DiemCongXetTuyen diem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Kiểm tra trùng dc_keys trước khi thêm
            if (isExistedKey(diem.getDcKeys())) {
                System.err.println("dc_keys đã tồn tại!");
                return false;
            }

            session.persist(diem); // chỉ thêm mới
            transaction.commit();
            return true;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa bản ghi điểm cộng theo ID
     */
    public boolean delete(int idDiemCong) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DiemCongXetTuyen diem = session.get(DiemCongXetTuyen.class, idDiemCong);
            if (diem != null) {
                session.remove(diem);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra sự tồn tại của dc_keys để tránh lỗi Duplicate Key trước khi lưu
     */
    public boolean isExistedKey(String dcKey) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(d.idDiemCong) FROM DiemCongXetTuyen d WHERE d.dcKeys = :key";
            Long count = session.createQuery(hql, Long.class)
                                .setParameter("key", dcKey)
                                .uniqueResult();
            return count != null && count > 0;
        }
    }
    
    // Thêm vào file DiemCongXetTuyenDAO.java
    public Object[] getThongTinUuTienByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t.khuVuc, t.doiTuong FROM ThiSinh t WHERE t.cccd = :code OR t.soBaoDanh = :code";
            Object[] result = session.createQuery(hql, Object[].class)
                          .setParameter("code", cccd)
                          .uniqueResult();
            if (result != null) {
                return result;
            }

            ThiSinh candidate = resolveThiSinhByCode(session, cccd);
            if (candidate == null) {
                return null;
            }
            return new Object[] { candidate.getKhuVuc(), candidate.getDoiTuong() };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public DiemCongXetTuyen getByCandidateCode(String code) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            DiemCongXetTuyen direct = session.createQuery(
                            "FROM DiemCongXetTuyen d WHERE d.tsCccd = :code",
                            DiemCongXetTuyen.class)
                    .setParameter("code", code)
                    .uniqueResult();
            if (direct != null) {
                return direct;
            }

            ThiSinh candidate = resolveThiSinhByCode(session, code);
            if (candidate == null || candidate.getCccd() == null) {
                return null;
            }

            return session.createQuery(
                            "FROM DiemCongXetTuyen d WHERE d.tsCccd = :code",
                            DiemCongXetTuyen.class)
                    .setParameter("code", candidate.getCccd())
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ThiSinh resolveThiSinhByCode(Session session, String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        ThiSinh candidate = session.createQuery("FROM ThiSinh t WHERE t.cccd = :code", ThiSinh.class)
                .setParameter("code", code)
                .uniqueResult();
        if (candidate != null) {
            return candidate;
        }
        return session.createQuery("FROM ThiSinh t WHERE t.soBaoDanh = :code", ThiSinh.class)
                .setParameter("code", code)
                .uniqueResult();
    }
}