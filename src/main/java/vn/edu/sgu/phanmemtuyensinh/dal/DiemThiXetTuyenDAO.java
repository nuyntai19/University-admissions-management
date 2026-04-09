package vn.edu.sgu.phanmemtuyensinh.dal;

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
            return session.createQuery("FROM DiemThiXetTuyen WHERE cccd = :cccd", DiemThiXetTuyen.class)
                    .setParameter("cccd", cccd).uniqueResult();
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
}
