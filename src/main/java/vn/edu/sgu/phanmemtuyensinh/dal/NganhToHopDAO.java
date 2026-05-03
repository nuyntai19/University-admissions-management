package vn.edu.sgu.phanmemtuyensinh.dal;

import org.hibernate.Session;
import org.hibernate.Transaction;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NganhToHop;
import vn.edu.sgu.phanmemtuyensinh.utils.HibernateUtil;
import java.util.List;

public class NganhToHopDAO {

    public List<NganhToHop> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM NganhToHop", NganhToHop.class).list();
        }
    }

    public List<NganhToHop> searchNganh(String keyword, int limit) {
        String key = "%" + keyword.toLowerCase() + "%";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM NganhToHop WHERE lower(maNganh) LIKE :key OR lower(tenNganhChuan) LIKE :key OR lower(maToHop) LIKE :key ORDER BY maNganh, maToHop",
                    NganhToHop.class)
                    .setParameter("key", key)
                    .setMaxResults(limit)
                    .list();
        }
    }

    public List<NganhToHop> getByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM NganhToHop WHERE maNganh = :ma", NganhToHop.class)
                    .setParameter("ma", maNganh).list();
        }
    }

    public NganhToHop getByTbKeys(String tbKeys) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM NganhToHop WHERE tbKeys = :k", NganhToHop.class)
                    .setParameter("k", tbKeys)
                    .uniqueResult();
        }
    }

    public boolean existsTbKeysExceptId(String tbKeys, int exceptId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(n) FROM NganhToHop n WHERE n.tbKeys = :k AND n.id <> :id",
                    Long.class)
                    .setParameter("k", tbKeys)
                    .setParameter("id", exceptId)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    public boolean add(NganhToHop nganhToHop) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(nganhToHop);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(NganhToHop nganhToHop) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(nganhToHop);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            NganhToHop nth = session.get(NganhToHop.class, id);
            if (nth != null) {
                session.remove(nth);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }
}
