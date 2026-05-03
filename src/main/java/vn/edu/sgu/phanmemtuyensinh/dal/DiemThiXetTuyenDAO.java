package vn.edu.sgu.phanmemtuyensinh.dal;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.utils.HibernateUtil;

public class DiemThiXetTuyenDAO {

    public List<DiemThiXetTuyen> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DiemThiXetTuyen", DiemThiXetTuyen.class).list();
        }
    }

    public DiemThiXetTuyen getById(int idDiemThi) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(DiemThiXetTuyen.class, idDiemThi);
        }
    }

    public List<DiemThiXetTuyen> getPage(int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DiemThiXetTuyen ORDER BY idDiemThi DESC", DiemThiXetTuyen.class)
                    .setFirstResult(offset)
                    .setMaxResults(pageSize)
                    .list();
        }
    }

    public List<DiemThiXetTuyen> getPageWithSort(int page, int pageSize, String sortOrder) {
        int offset = Math.max(0, (page - 1) * pageSize);
        String orderClause = "ASC".equals(sortOrder) ? "ORDER BY idDiemThi ASC" : "ORDER BY idDiemThi DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DiemThiXetTuyen " + orderClause, DiemThiXetTuyen.class)
                    .setFirstResult(offset)
                    .setMaxResults(pageSize)
                    .list();
        }
    }

    public long countAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long total = session.createQuery("SELECT COUNT(d) FROM DiemThiXetTuyen d", Long.class)
                    .uniqueResult();
            return total == null ? 0 : total;
        }
    }

    public DiemThiXetTuyen getByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DiemThiXetTuyen WHERE cccd = :code OR soBaoDanh = :code ORDER BY idDiemThi DESC", DiemThiXetTuyen.class)
                    .setParameter("code", cccd)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    public DiemThiXetTuyen getByCcqdAndPhuongThuc(String cccd, String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM DiemThiXetTuyen WHERE (cccd = :code OR soBaoDanh = :code) AND phuongThuc = :pt", 
                    DiemThiXetTuyen.class)
                    .setParameter("code", cccd)
                    .setParameter("pt", phuongThuc)
                    .uniqueResult();
        }
    }

    public List<DiemThiXetTuyen> getByPhuongThuc(String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DiemThiXetTuyen WHERE phuongThuc = :pt", DiemThiXetTuyen.class)
                    .setParameter("pt", phuongThuc).list();
        }
    }

    public List<DiemThiXetTuyen> searchByKeyword(String keyword, int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        String key = "%" + keyword.toLowerCase() + "%";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM DiemThiXetTuyen d "
                                    + "WHERE lower(d.cccd) LIKE :key OR lower(d.soBaoDanh) LIKE :key "
                                    + "ORDER BY d.idDiemThi DESC",
                            DiemThiXetTuyen.class)
                    .setParameter("key", key)
                    .setFirstResult(offset)
                    .setMaxResults(pageSize)
                    .list();
        }
    }

    public long countByKeyword(String keyword) {
        String key = "%" + keyword.toLowerCase() + "%";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long total = session.createQuery(
                            "SELECT COUNT(d) FROM DiemThiXetTuyen d "
                                    + "WHERE lower(d.cccd) LIKE :key OR lower(d.soBaoDanh) LIKE :key",
                            Long.class)
                    .setParameter("key", key)
                    .uniqueResult();
            return total == null ? 0 : total;
        }
    }

    public boolean add(DiemThiXetTuyen diem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(diem);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ignored) {
                }
            }
            System.err.println("Lỗi thêm điểm thi: " + e.getMessage());
            return false;
        }
    }

    public boolean update(DiemThiXetTuyen diem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(diem);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ignored) {
                }
            }
            System.err.println("Lỗi cập nhật điểm thi: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int idDiemThi) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DiemThiXetTuyen diem = session.get(DiemThiXetTuyen.class, idDiemThi);
            if (diem != null) {
                session.remove(diem);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ignored) {
                }
            }
            System.err.println("Lỗi xóa điểm thi: " + e.getMessage());
            return false;
        }
    }

    public java.util.Set<String> getAllCccd() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<String> cccdList = session.createQuery("SELECT d.cccd FROM DiemThiXetTuyen d", String.class).list();
            return new java.util.HashSet<>(cccdList);
        }
    }

    public java.util.Map<String, DiemThiXetTuyen> getAllDiemMap() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<DiemThiXetTuyen> list = session.createQuery("FROM DiemThiXetTuyen", DiemThiXetTuyen.class).list();
            java.util.Map<String, DiemThiXetTuyen> map = new java.util.HashMap<>();
            for (DiemThiXetTuyen d : list) {
                map.put(d.getCccd(), d);
            }
            return map;
        }
    }
    
    public BigDecimal tinhDiemGoc(String cccd, String maToHop, String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            // 1. Lấy điểm thí sinh
            DiemThiXetTuyen d = session.createQuery(
                    "FROM DiemThiXetTuyen WHERE cccd = :code OR soBaoDanh = :code", DiemThiXetTuyen.class)
                    .setParameter("code", cccd)
                    .uniqueResult();

            if (d == null) return BigDecimal.ZERO;

            // 2. Lấy tổ hợp môn
            Object[] mon = session.createQuery(
                    "SELECT t.mon1, t.mon2, t.mon3 FROM ToHopMon t WHERE t.maToHop = :ma",
                    Object[].class)
                    .setParameter("ma", maToHop)
                    .uniqueResult();

            if (mon == null) return BigDecimal.ZERO;

            BigDecimal tong = BigDecimal.ZERO;

            // 3. Map môn → điểm (THEO ENTITY CỦA BẠN)
            java.util.Map<String, BigDecimal> map = new java.util.HashMap<>();
            map.put("TO", d.getTo());
            map.put("LI", d.getLi());
            map.put("HO", d.getHo());
            map.put("SI", d.getSi());
            map.put("SU", d.getSu());
            map.put("DI", d.getDi());
            map.put("VA", d.getVa());
            map.put("GDCD", d.getGdcd());
            map.put("N1", d.getN1Thi()); // nếu dùng ngoại ngữ
            map.put("TI", d.getTi());
            map.put("KTPL", d.getKtpl());
            map.put("NL1", d.getNl1());
            // thêm nếu cần

            // 4. Tính tổng theo tổ hợp
            for (Object m : mon) {
                String tenMon = (String) m;
                BigDecimal diem = map.get(tenMon);
                if (diem != null) tong = tong.add(diem);
            }

            return tong;

        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }
}
