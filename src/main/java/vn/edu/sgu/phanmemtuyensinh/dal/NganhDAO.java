package vn.edu.sgu.phanmemtuyensinh.dal;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import vn.edu.sgu.phanmemtuyensinh.dal.entity.Nganh;
import vn.edu.sgu.phanmemtuyensinh.utils.HibernateUtil;

public class NganhDAO {

    public List<Nganh> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Nganh", Nganh.class).list();
        }
    }

    public Nganh getByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Nganh WHERE maNganh = :ma", Nganh.class)
                    .setParameter("ma", maNganh).uniqueResult();
        }
    }

    public Nganh getById(int idNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Nganh.class, idNganh);
        }
    }

    public List<Nganh> searchByKeyword(String keyword) {
        String key = "%" + keyword.toLowerCase() + "%";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Nganh n WHERE lower(n.maNganh) LIKE :key OR lower(n.tenNganh) LIKE :key ORDER BY n.idNganh DESC",
                            Nganh.class)
                    .setParameter("key", key)
                    .list();
        }
    }

    public boolean add(Nganh nganh) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(nganh);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Nganh nganh) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(nganh);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idNganh) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Nganh nganh = session.get(Nganh.class, idNganh);
            if (nganh != null) {
                session.remove(nganh);
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

    public boolean clearAndResetId() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createNativeQuery("TRUNCATE TABLE xt_nganh").executeUpdate();
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }
}
