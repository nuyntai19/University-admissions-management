package vn.edu.sgu.phanmemtuyensinh.dal;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;
import vn.edu.sgu.phanmemtuyensinh.utils.HibernateUtil;

public class ThiSinhDAO {

    public List<ThiSinh> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM ThiSinh", ThiSinh.class).list();
        }
    }

    public ThiSinh getById(int idThiSinh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(ThiSinh.class, idThiSinh);
        }
    }

    public List<ThiSinh> getPage(int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM ThiSinh ORDER BY idThiSinh DESC", ThiSinh.class)
                    .setFirstResult(offset)
                    .setMaxResults(pageSize)
                    .list();
        }
    }

    public long countAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long total = session.createQuery("SELECT COUNT(ts) FROM ThiSinh ts", Long.class)
                    .uniqueResult();
            return total == null ? 0 : total;
        }
    }

    public ThiSinh getByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM ThiSinh WHERE cccd = :cccd", ThiSinh.class)
                    .setParameter("cccd", cccd).uniqueResult();
        }
    }

    public List<ThiSinh> searchByKeyword(String keyword, int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        String key = "%" + keyword.toLowerCase() + "%";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM ThiSinh ts "
                                    + "WHERE lower(ts.cccd) LIKE :key "
                                    + "OR lower(ts.ho) LIKE :key "
                                    + "OR lower(ts.ten) LIKE :key "
                                    + "OR lower(concat(ts.ho, ' ', ts.ten)) LIKE :key "
                                    + "ORDER BY ts.idThiSinh DESC",
                            ThiSinh.class)
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
                            "SELECT COUNT(ts) FROM ThiSinh ts "
                                    + "WHERE lower(ts.cccd) LIKE :key "
                                    + "OR lower(ts.ho) LIKE :key "
                                    + "OR lower(ts.ten) LIKE :key "
                                    + "OR lower(concat(ts.ho, ' ', ts.ten)) LIKE :key",
                            Long.class)
                    .setParameter("key", key)
                    .uniqueResult();
            return total == null ? 0 : total;
        }
    }

    public List<ThiSinh> searchByHoTen(String hoTen) {
        return searchByKeyword(hoTen, 1, Integer.MAX_VALUE);
    }

    public boolean add(ThiSinh thiSinh) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(thiSinh);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ignored) {
                }
            }
            System.err.println("Lỗi thêm thí sinh: " + e.getMessage());
            return false;
        }
    }

    public boolean update(ThiSinh thiSinh) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(thiSinh);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ignored) {
                }
            }
            System.err.println("Lỗi cập nhật thí sinh: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int idThiSinh) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ThiSinh ts = session.get(ThiSinh.class, idThiSinh);
            if (ts != null) {
                session.remove(ts);
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
            System.err.println("Lỗi xóa thí sinh: " + e.getMessage());
            return false;
        }
    }
}
